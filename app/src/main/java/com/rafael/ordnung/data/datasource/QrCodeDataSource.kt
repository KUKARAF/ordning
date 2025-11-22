package com.rafael.ordnung.data.datasource

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.LuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.ReaderException
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.qrcode.QRCodeMultiReader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.rendering.PDFRenderer
import java.io.ByteArrayInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QrCodeDataSource @Inject constructor() {
    
    suspend fun extractQrCodesFromPdf(fileBytes: ByteArray): List<String> {
        val qrCodes = mutableListOf<String>()
        
        try {
            val document = PDDocument.load(ByteArrayInputStream(fileBytes))
            val renderer = PDFRenderer(document)
            
            // Render each page and scan for QR codes
            for (pageIndex in 0 until document.numberOfPages) {
                val image = renderer.renderImageWithDPI(pageIndex, 300f)
                val bitmap = convertToBitmap(image as Any)
                
                val qrCodeData = scanQrCodes(bitmap)
                qrCodes.addAll(qrCodeData)
            }
            
            document.close()
        } catch (e: Exception) {
            // Log error but don't fail the entire parsing
        }
        
        return qrCodes
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun convertToBitmap(image: Any): Bitmap {
        // For Android-compatible PDFBox, try to handle both AWT and Android bitmap scenarios
        return try {
            // If it's already an Android Bitmap (some PDFBox Android versions support this)
            image as? Bitmap ?: run {
                // Fallback for AWT BufferedImage (shouldn't happen with Android PDFBox but just in case)
                val bufferedImage = image as java.awt.image.BufferedImage
                val width = bufferedImage.width
                val height = bufferedImage.height
                
                val pixels = IntArray(width * height)
                bufferedImage.getRGB(0, 0, width, height, pixels, 0, width)
                
                Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
            }
        } catch (e: Exception) {
            // Last resort - create a placeholder bitmap
            Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        }
    }
    
    private fun scanQrCodes(bitmap: Bitmap): List<String> {
        val qrCodes = mutableListOf<String>()
        
        try {
            val intArray = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            
            val source: LuminanceSource = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            
            val reader = QRCodeMultiReader()
            val hints = mapOf<DecodeHintType, Any>(
                DecodeHintType.TRY_HARDER to true,
                DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE)
            )
            
            val results = reader.decodeMultiple(binaryBitmap, hints)
            for (result in results) {
                qrCodes.add(result.text)
            }
            
        } catch (e: ReaderException) {
            // No QR code found on this page
        } catch (e: Exception) {
            // Other errors during QR code scanning
        }
        
        return qrCodes
    }
    
    suspend fun extractQrCodeFromImage(imageBytes: ByteArray): String? {
        return try {
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            val qrCodes = scanQrCodes(bitmap)
            qrCodes.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }
    
    fun isValidQrCode(data: String): Boolean {
        // Basic validation for common ticket QR code formats
        return when {
            // Deutsche Bahn format
            data.startsWith("OTP") || data.contains("bahn.de") -> true
            // Generic URL format
            data.startsWith("http://") || data.startsWith("https://") -> true
            // JSON format (common in modern tickets)
            data.startsWith("{") && data.endsWith("}") -> true
            // Base64 encoded data
            data.matches(Regex("^[A-Za-z0-9+/]+={0,2}$")) && data.length > 20 -> true
            // Minimum length check
            data.length > 10 -> true
            else -> false
        }
    }
}