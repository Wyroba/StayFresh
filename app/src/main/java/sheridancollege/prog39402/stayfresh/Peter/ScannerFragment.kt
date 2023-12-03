package sheridancollege.prog39402.stayfresh.Peter

import android.Manifest
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import sheridancollege.prog39402.stayfresh.R
import sheridancollege.prog39402.stayfresh.databinding.FragmentScannerBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ScannerFragment : Fragment() {

    private val pantryViewModel: PantryViewModel by viewModels()

    val functions = FirebaseFunctions.getInstance()

    private lateinit var imageCapture: ImageCapture

    private lateinit var progressDialog: ProgressDialog
    private lateinit var textRecognizer: TextRecognizer
    private var imageUri: Uri? = null

    private val cameraProviderFuture by lazy { ProcessCameraProvider.getInstance(requireContext()) }

    private lateinit var datePickerDialog: DatePickerDialog
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    private var _binding: FragmentScannerBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraPermissions: Array<String>
    private lateinit var storagePermissions: Array<String>

    private val auth = Firebase.auth

    private lateinit var outputDirectory: File

    companion object {
        private const val TAG = "ScannerFragment"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = arguments
        if (bundle == null) {
            Log.d("Confirmation", "Fragment 2 didn't receive any info")
            return
        }
        val args = ScannerFragmentArgs.fromBundle(bundle)

        outputDirectory = getOutputDirectory()

        binding.takeImage.visibility = View.GONE

        cameraPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        binding.inputImageBtn.setOnClickListener {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS
                )
            }
        }

        binding.takeImage.setOnClickListener {
            takePhoto()
        }

        binding.viewFinder.setOnClickListener {
            takePhoto()
        }

        val description = args.description
        binding.testText.setText(description)
        val category = args.category
        binding.categoryText.setText(category)
        val image = args.image

        binding.AddToFridgeButton.setOnClickListener {

            val catText = binding.testText.text.toString()
            val descText = binding.categoryText.text.toString()

            val expText = binding.expirationText.text.toString()

            val expDate = dateFormat.parse(expText)

            if (expDate == null) {
                showToast("Invalid date format. Please use dd-MM-yyyy.")
                return@setOnClickListener
            }


            val userId = auth.currentUser?.uid
            if (userId == null) {
                Log.d(TAG, "Error: no user signed in.")
                return@setOnClickListener
            }

            val foodItem = Food(
                description = catText,
                category = descText,
                expirationDate = Timestamp(expDate),
                categoryImage = image.toString()
            )

            pantryViewModel.addFoodItem(userId, foodItem)
            Log.d(TAG, "Document added successfully.")
            findNavController().navigate(R.id.action_scannerFragment_to_contentFragment)

        }

        val calendar = Calendar.getInstance()
        datePickerDialog = DatePickerDialog(requireContext(), { _, year, monthOfYear, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(Calendar.YEAR, year)
            selectedDate.set(Calendar.MONTH, monthOfYear)
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            binding.expirationText.setText(dateFormat.format(selectedDate.time))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        calendar.add(Calendar.DAY_OF_YEAR, 7)
        binding.expirationText.setText(dateFormat.format(calendar.time))

        binding.expirationBtn.setOnClickListener {
            datePickerDialog.show()
        }

    }

    private fun takePhoto() {
        // Create timestamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Log.d(TAG, msg)

                    // Load the captured image into a Bitmap
                    val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, savedUri)
                    // Now call recognizeText with the captured bitmap
                    recognizeText(bitmap)
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }
            }
        )
    }

    private fun recognizeText(bitmap: Bitmap) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        textRecognizer.process(inputImage)
            .addOnSuccessListener { text ->
                // Handle successful text recognition here
                if (!text.text.isNullOrBlank()) {
                    val detectedText = text.text.trim()
                    Log.d("RecognizedText", "Recognized Text: $detectedText")
                    callOpenAIWithUserInput(detectedText) // Update expirationText with recognized text
                }
                else
                {
                    showToast("Did not find any date in the image.")
                }
            }
            .addOnFailureListener { exception ->
                // Handle failed text recognition here
                showToast("Text Recognition failed: ${exception.message}")
                Log.d("RecognizedText", "Failed")
            }
    }

    private fun callOpenAIWithUserInput(userInput: String) {
        // Initialize the Cloud Function call with the user input as an argument
        functions.getHttpsCallable("callOpenAIForImageDateParse")
            .call(mapOf("userInput" to userInput))
            .addOnCompleteListener { task ->

                if (!task.isSuccessful) {
                    val e = task.exception
                }

                val userDateStr = (task.result?.data as? String)
                Log.d("RecognizedText", "Recognized Text: $userDateStr")

                if (isValidDate(userDateStr)) {
                    val updatedDate = updateYearToCurrentOrNext(userDateStr.toString())
                    Log.d("RecognizedText", "Updated Date: $updatedDate")
                    // Update expirationText with recognized text after userDate is validated
                    binding.expirationText.setText(updatedDate)
                } else {
                    Log.d("RecognizedText", "Text is not in the correct format: $userDateStr")
                    showToast("Failed to find date in the correct format")
                }

            }
    }

    private fun isValidDate(date: String?): Boolean {
        // Regular expression to match the format DD-MM-YYYY
        val regex = """\d{2}-\d{2}-\d{4}""".toRegex()
        return date?.matches(regex) == true
    }

    private fun updateYearToCurrentOrNext(dateStr: String): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val recognizedDate = sdf.parse(dateStr) ?: return dateStr

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)

        calendar.time = recognizedDate
        calendar.set(Calendar.YEAR, currentYear)

        val currentDate = Calendar.getInstance()
        if (calendar.before(currentDate)) {
            calendar.add(Calendar.YEAR, 1)
        }

        return sdf.format(calendar.time)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
            binding.takeImage.visibility = View.VISIBLE
            binding.inputImageBtn.visibility = View.GONE
            binding.imageIv.visibility = View.GONE
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

        imageCapture = ImageCapture.Builder()
            .build()

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = requireContext().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir
        else
            requireContext().filesDir
    }

}