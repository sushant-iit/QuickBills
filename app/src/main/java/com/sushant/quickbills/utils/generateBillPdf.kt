package com.sushant.quickbills.utils

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.itextpdf.io.image.ImageData
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.*
import com.itextpdf.layout.property.BorderRadius
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.VerticalAlignment
import com.sushant.quickbills.data.*
import com.sushant.quickbills.model.Bill
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileNotFoundException

fun createOrShowBillPDF(context: Context, bill: Bill): Job {
    val database = Firebase.database.reference
    val auth = Firebase.auth
    val storage = Firebase.storage.reference

    val storagePath = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    val fileName = bill.customerDetails!!.number + "T" + billCreationDateAndTime(bill)

    //If bill PDF is already generated .. then directly show:
    val file = File(storagePath, "${fileName}.pdf")

    return GlobalScope.launch {
        if (!file.exists()) {
            //First get the brand details and logoImage of the user
            var brandInfo: HashMap<*, *>? = null
            var imageBytes: ByteArray? = null
            val getBrandInfoJob = launch {
                val brandSnapshot =
                    database.child(USERS_FIELD).child(auth.currentUser!!.uid).get().await()
                if (brandSnapshot.value != null)
                    brandInfo = brandSnapshot.value as HashMap<*, *>
            }
            val getLogoImageJob = launch {
                imageBytes = try {
                    storage.child("logos/${auth.currentUser!!.uid}.jpg")
                        .getBytes(MAX_IMAGE_DOWNLOAD_SIZE).await()
                } catch (e: Throwable) {
                    //If the user uploaded image is not found, load the default image
                    try {
                        storage.child("sample_logo.png")
                            .getBytes(MAX_IMAGE_DOWNLOAD_SIZE).await()
                    } catch (e: Throwable) {
                        Log.d("code", e.message.toString())
                        null
                    }
                }
            }
            getBrandInfoJob.join()
            getLogoImageJob.join()

            //Check for errors like network errors
            if (brandInfo == null) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Something Went Wrong!!", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            generatePDF(bill, file, brandInfo!!, imageBytes)
        }
        viewPDF(context, file)
    }

}

private fun generatePDF(bill: Bill, file: File, brandInfo: HashMap<*, *>, imageBytes: ByteArray?) {
    val auth = Firebase.auth
    try {
        val pdfWriter = PdfWriter(file)
        val pdfDocument = PdfDocument(pdfWriter)
        pdfDocument.defaultPageSize = PageSize.A4
        val document = Document(pdfDocument)

        //Dividing document into two parts for adding top level entries------------------------------------
        //-------------------------------Main Heading of Invoice and Shop Name
        val logoImageData: ImageData?
        var logoImage: Image? = null
        if (imageBytes != null) {
            logoImageData = ImageDataFactory.create(imageBytes)
            logoImage = Image(logoImageData).setHeight(80F).setWidth(80F).setBorderRadius(
                BorderRadius(15F)
            )
        }
        val invoiceHead =
            Paragraph().add("INVOICE").setFontSize(25f)
                .setBold().setTextAlignment(TextAlignment.RIGHT)
        val brandName = Text("${brandInfo[BRAND_NAME_FIELD]}\n").setBold().setFontSize(15F)
        val mobiLeHead = Text("M: ").setBold()
        val emailHead = Text("E: ").setBold()
        val addressHead = Text("A: ").setBold()
        val dateHead = Text("D: ").setBold()
        val brandMobileNo = Text("${brandInfo[BRAND_NUMBER_FIELD]}\n").setItalic()
        val brandEmail =
            Link(
                "${auth.currentUser!!.email}\n",
                PdfAction.createURI("mailto:${auth.currentUser!!.email}")
            ).setItalic()
        val brandAddress = Text("${brandInfo[BRAND_ADDRESS_FIELD]}\n")
        val shopInfo =
            Paragraph().add(brandName)
                .add(mobiLeHead).add(brandMobileNo)
                .add(emailHead).add(brandEmail)
                .add(addressHead).add(brandAddress)
                .setTextAlignment(TextAlignment.RIGHT)
        //------------------------------Setting Invoice Details------------------------------------
        val columnWidthCustomers = arrayOf(280.0F, 280.0F).toFloatArray()
        val tableHead = Table(columnWidthCustomers)
        val invoiceToHead = Text("INVOICE TO:\n").setBold().setFontSize(10F)
        val customerName =
            Text("${bill.customerDetails!!.name}\n").setFontColor(DeviceRgb(11, 81, 89)).setBold()
                .setFontSize(15F)
        val mobileNo = Text("${bill.customerDetails!!.number}\n").setItalic()
        val customerAddress = Text("${bill.customerDetails!!.address}\n")
        val billDate = Text("${billCreationDateAndTime(bill)}\n")
        val customerParagraph = Paragraph()
            .add("\n").add(invoiceToHead).add(customerName).add(mobiLeHead).add(mobileNo)
            .add(addressHead)
            .add(customerAddress).add(dateHead).add(billDate)
        val totalDueHead = Text("Total Due:\n").setBold()
        val totalAmount =
            Text("\u20B9 ${bill.totalAmount.toString()}").setUnderline()
                .setFontSize(25f)
        val dueParagraph =
            Paragraph().add("\n").add(totalDueHead).add(totalAmount)
                .setTextAlignment(TextAlignment.RIGHT)
        val totalPaidHead = Text("Amount Paid:\n").setBold()
        val totalPaid =
            Text("\u20B9 0").setUnderline()
                .setFontSize(25f)
        val totalPaidParagraph =
            Paragraph().add("\n").add(totalPaidHead).add(totalPaid)
        if (logoImage != null) {
            tableHead.addCell(Cell().add(logoImage).setBorder(Border.NO_BORDER))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
        } else {
            tableHead.addCell(Cell().setBorder(Border.NO_BORDER))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
        }
        tableHead.addCell(
            Cell().add(shopInfo).setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER)
        )
        tableHead.addCell(Cell().add(customerParagraph).setBorder(Border.NO_BORDER))
        tableHead.addCell(
            Cell().add(invoiceHead).setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER)
        )

        tableHead.addCell(Cell().add(totalPaidParagraph).setBorder(Border.NO_BORDER))
        tableHead.addCell(Cell().add(dueParagraph).setBorder(Border.NO_BORDER))
        //-----------------------------------------------------------------------------------------

        //Adding Particulars-----------------------------------------------------------------------
        val columnWidthParticulars = arrayOf(65.88F, 197.64F, 98.82F, 98.82F, 98.82F).toFloatArray()
        val tableParticulars = Table(columnWidthParticulars)
        //--------------------------Setting up table heading-----
        val serialHead = Text("Sno.").setBold()
        val itemHead = Text("Item Description").setBold()
        val rateHead = Text("Unit Price").setBold()
        val quantityHead = Text("Qty").setBold()
        val amountHead = Text("Amount").setBold()
        tableParticulars.addCell(
            Cell().add(Paragraph().add(serialHead).setTextAlignment(TextAlignment.CENTER))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER)
                .setBorderTop(SolidBorder(1F))
                .setBorderBottom(SolidBorder(1F))
                .setBackgroundColor(DeviceRgb(231, 231, 231))
        )
        tableParticulars.addCell(
            Cell().add(Paragraph().add(itemHead).setTextAlignment(TextAlignment.LEFT))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER)
                .setBorderTop(SolidBorder(1F))
                .setBorderBottom(SolidBorder(1F))
                .setBackgroundColor(DeviceRgb(231, 231, 231))
        )
        tableParticulars.addCell(
            Cell().add(Paragraph().add(rateHead).setTextAlignment(TextAlignment.RIGHT))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER)
                .setBorderTop(SolidBorder(1F))
                .setBorderBottom(SolidBorder(1F))
        )
        tableParticulars.addCell(
            Cell().add(Paragraph().add(quantityHead).setTextAlignment(TextAlignment.RIGHT))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER)
                .setBorderTop(SolidBorder(1F))
                .setBorderBottom(SolidBorder(1F))
        )
        tableParticulars.addCell(
            Cell().add(Paragraph().add(amountHead).setTextAlignment(TextAlignment.RIGHT))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER)
                .setBorderTop(SolidBorder(1F))
                .setBorderBottom(SolidBorder(1F))
        )
        //Iterate through all the particulars and add to the pdf
        var count = 1
        if (bill.particulars != null) {
            for (item in bill.particulars!!) {
                val serial = Text(count.toString())
                val itemName = Text(item.itemName)
                val rate = Text(item.itemPrice.toString())
                val quantity = Text(item.itemQty.toString())
                val amount = Text(item.itemAmount.toString()).setBold()
                count++
                tableParticulars.addCell(
                    Cell().add(Paragraph().add(serial).setTextAlignment(TextAlignment.CENTER))
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setBorder(Border.NO_BORDER)
                )
                tableParticulars.addCell(
                    Cell().add(Paragraph().add(itemName).setTextAlignment(TextAlignment.LEFT))
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setBorder(Border.NO_BORDER)
                )
                tableParticulars.addCell(
                    Cell().add(Paragraph().add(rate).setTextAlignment(TextAlignment.RIGHT))
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setBorder(Border.NO_BORDER)
                        .setBackgroundColor(DeviceRgb(231, 231, 231))
                )
                tableParticulars.addCell(
                    Cell().add(Paragraph().add(quantity).setTextAlignment(TextAlignment.RIGHT))
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setBorder(Border.NO_BORDER)
                        .setBackgroundColor(DeviceRgb(231, 231, 231))
                )
                tableParticulars.addCell(
                    Cell().add(Paragraph().add(amount).setTextAlignment(TextAlignment.RIGHT))
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setBorder(Border.NO_BORDER)
                        .setBackgroundColor(DeviceRgb(231, 231, 231))
                )
            }
        }
        //Finally show the total amount
        tableParticulars.addCell(
            Cell(0, 4).add(
                Paragraph().add("Total Amount:").setBold()
                    .setTextAlignment(TextAlignment.RIGHT)
            )
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER)
                .setBorderTop(SolidBorder(1F))
                .setBorderBottom(SolidBorder(1F))
                .setBorderRight(SolidBorder(1F))
        )
        tableParticulars.addCell(
            Cell().add(
                Paragraph().add(bill.totalAmount.toString()).setBold()
                    .setTextAlignment(TextAlignment.RIGHT)
            )
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER)
                .setBorderTop(SolidBorder(1F))
                .setBorderBottom(SolidBorder(1F))
        )
        //-------------------------------------------------------------------------------------------

        //Setting Up Thank You Message---------------------------------------------------------------
        val thanksText = Text("Thank you for your business!\n").setFontColor(DeviceRgb(49, 62, 84))
            .setFontSize(15F)
        val concludeText = Text("Should you have any queries, please contact us.").setFontColor(
            DeviceRgb(
                49,
                62,
                84
            )
        )
            .setFontSize(8F)
        val thanksPara =
            Paragraph().add(thanksText).add(concludeText).setTextAlignment(TextAlignment.CENTER)

        document.add(tableHead)
        document.add(Paragraph().add("\n"))
        document.add(tableParticulars)
        document.add(Paragraph().add("\n"))
        document.add(thanksPara)
        document.close()
    } catch (e: FileNotFoundException) {
        Log.e("PdfCreateError", e.message.toString())
    }
}

private fun viewPDF(context: Context, file: File) {
    val data = FileProvider.getUriForFile(context, "com.sushant.fileProvider", file)
    val viewPDFIntent = Intent(Intent.ACTION_VIEW)
    viewPDFIntent.flags = FLAG_GRANT_READ_URI_PERMISSION
    viewPDFIntent.setDataAndType(data, "application/pdf")
    context.startActivity(viewPDFIntent)
}
