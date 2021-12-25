package com.sukhtaraitint.ticketing_system

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.tejpratapsingh.pdfcreator.activity.PDFCreatorActivity
import com.tejpratapsingh.pdfcreator.utils.PDFUtil
import com.tejpratapsingh.pdfcreator.views.PDFBody
import com.tejpratapsingh.pdfcreator.views.PDFFooterView
import com.tejpratapsingh.pdfcreator.views.PDFHeaderView
import java.io.File
import java.lang.Exception
import android.text.Html

import android.text.Spanned

import com.tejpratapsingh.pdfcreator.views.basic.PDFTextView
import android.content.Intent
import android.net.Uri
import com.tejpratapsingh.pdfcreator.activity.PDFViewerActivity
import android.R
import android.graphics.Color

import android.view.Gravity

import android.view.ViewGroup

import android.widget.FrameLayout

import com.tejpratapsingh.pdfcreator.views.basic.PDFImageView

import android.widget.LinearLayout

import androidx.core.text.HtmlCompat

import com.tejpratapsingh.pdfcreator.views.basic.PDFLineSeparatorView

import com.tejpratapsingh.pdfcreator.views.PDFTableView

import com.tejpratapsingh.pdfcreator.views.basic.PDFPageBreakView

import android.graphics.Typeface

import android.text.Spannable

import android.text.style.ForegroundColorSpan

import android.text.SpannableString
import android.widget.ImageView
import androidx.annotation.Nullable
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sukhtaraitint.ticketing_system.models.TicketSold
import com.sukhtaraitint.ticketing_system.models.TotalTicketSoldReport
import com.sukhtaraitint.ticketing_system.utils.ConstantValues
import com.tejpratapsingh.pdfcreator.views.PDFTableView.PDFTableRowView

import com.tejpratapsingh.pdfcreator.views.basic.PDFHorizontalView
import java.lang.String
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors


var totalTicketCount: Int? = 0
var perTicketPrice: Double? = 0.14
var ticketSoldReportCounterWise: TotalTicketSoldReport? = null
var ticketSoldReportList: MutableList<TotalTicketSoldReport>? = mutableListOf(TotalTicketSoldReport())
var ticketSoldReportCountObj: ValueEventListener? = null

class BillGenerateActivity : PDFCreatorActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = Firebase.database(ConstantValues.DB_URL)
        val ticketSoldReportRef = database.getReference("daily_sell_report")
        Executors.newSingleThreadExecutor().execute(Runnable {
            runOnUiThread{
                ticketSoldReportCountObj = object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("DataSnapshot", snapshot.getValue().toString())
                        if (snapshot.getValue() != null){
                            val ticketSoldCounterSet = snapshot.getValue() as Map<kotlin.String, *>
                            var totalTicketSoldCount = 0
                            for ((key, value) in ticketSoldCounterSet) {

                                val ticketSoldMap: Map<kotlin.String, *> = value as Map<kotlin.String, *>
                                var counterSellCount = 0

                                ticketSoldReportCounterWise = TotalTicketSoldReport(
                                    hashMapOf(),
                                    ticketSoldMap.get("total_tickets").toString().toInt(),
                                    ticketSoldMap.get("date_time").toString().toLong(),
                                    ticketSoldMap.get("report_taken_by").toString())
                                ticketSoldReportList!!.add(ticketSoldReportCounterWise!!)

                                totalTicketSoldCount = totalTicketSoldCount + ticketSoldMap.get("total_tickets").toString().toInt()

                            }
                        }

                        ticketSoldReportRef.removeEventListener(ticketSoldReportCountObj!!)

                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                        Log.d("DataSnapshot", error.toString())
                    }

                }
                ticketSoldReportRef.addValueEventListener(ticketSoldReportCountObj!!)
            }
        })

        val pdfUtilListener: PDFUtil.PDFUtilListener = object : PDFUtil.PDFUtilListener {
            override fun pdfGenerationSuccess(savedPDFFile: File?) {
                Log.d("PDFCreatorActivity", "PDF file created: " + savedPDFFile!!.absolutePath.toString())
                val pdfUri: Uri = Uri.fromFile(savedPDFFile)

                val intentPdfViewer = Intent(applicationContext, PDFViewerActivity::class.java)
                intentPdfViewer.putExtra(PDFViewerActivity.PDF_FILE_URI, pdfUri)

                startActivity(intentPdfViewer)
            }

            override fun pdfGenerationFailure(exception: Exception?) {
                TODO("Not yet implemented")
            }
        }

        createPDF("Billing", pdfUtilListener)
    }

    override fun getHeaderView(pageIndex: Int): PDFHeaderView? {
        val headerView = PDFHeaderView(applicationContext)
        val horizontalView = PDFHorizontalView(applicationContext)
        val pdfTextView = PDFTextView(applicationContext, PDFTextView.PDF_TEXT_SIZE.HEADER)
        val word = SpannableString("INVOICE")
        word.setSpan(
            ForegroundColorSpan(Color.DKGRAY),
            0,
            word.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        pdfTextView.text = word
        pdfTextView.setLayout(
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1f
            )
        )
        pdfTextView.view.gravity = Gravity.CENTER_VERTICAL
        pdfTextView.view.setTypeface(pdfTextView.view.typeface, Typeface.BOLD)
        horizontalView.addView(pdfTextView)
        val imageView = PDFImageView(applicationContext)
        val imageLayoutParam = LinearLayout.LayoutParams(
            60,
            60, 0f
        )
        imageView.setImageScale(ImageView.ScaleType.CENTER_INSIDE)
        imageView.setImageResource(com.sukhtaraitint.ticketing_system.R.drawable.sit_logo)
        imageLayoutParam.setMargins(0, 0, 10, 0)
        imageView.setLayout(imageLayoutParam)
        horizontalView.addView(imageView)
        headerView.addView(horizontalView)
        val lineSeparatorView1 =
            PDFLineSeparatorView(applicationContext).setBackgroundColor(Color.WHITE)
        headerView.addView(lineSeparatorView1)
        return headerView
    }

    override fun getBodyViews(): PDFBody? {
        val pdfBody = PDFBody()
        val pdfCompanyNameView = PDFTextView(applicationContext, PDFTextView.PDF_TEXT_SIZE.H3)
        pdfCompanyNameView.setText("Sukhtara International Ltd. ")
        pdfBody.addView(pdfCompanyNameView)
        val lineSeparatorView1 =
            PDFLineSeparatorView(applicationContext).setBackgroundColor(Color.WHITE)
        pdfBody.addView(lineSeparatorView1)
        val pdfAddressView = PDFTextView(applicationContext, PDFTextView.PDF_TEXT_SIZE.P)
        pdfAddressView.setText("Nikunja\nDhaka - 1256")
        pdfBody.addView(pdfAddressView)
        val lineSeparatorView2 =
            PDFLineSeparatorView(applicationContext).setBackgroundColor(Color.WHITE)
        lineSeparatorView2.setLayout(
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                8, 0f
            )
        )
        pdfBody.addView(lineSeparatorView2)
        val lineSeparatorView3 =
            PDFLineSeparatorView(applicationContext).setBackgroundColor(Color.WHITE)
        pdfBody.addView(lineSeparatorView3)
        val widthPercent = intArrayOf(20, 20, 20, 40) // Sum should be equal to 100%
        val textInTable = arrayOf("তারিখঃ", "বিবরণঃ", "টিকেট বিক্রয়ঃ", "প্রতি টিকেট বিলঃ","মোট বিলঃ")
        val pdfTableTitleView = PDFTextView(applicationContext, PDFTextView.PDF_TEXT_SIZE.P)
        pdfTableTitleView.setText("Total Ticket Sell Report")
        pdfBody.addView(pdfTableTitleView)
//        val pdfPageBreakView = PDFPageBreakView(applicationContext)
//        pdfBody.addView(pdfPageBreakView)
        val tableHeader = PDFTableRowView(applicationContext)
        for (s in textInTable) {
            val pdfTextView = PDFTextView(applicationContext, PDFTextView.PDF_TEXT_SIZE.P)
            pdfTextView.setText("$s")
            tableHeader.addToRow(pdfTextView)
        }

        // todo: add data here with real data.
        val tableRowView1 = PDFTableRowView(applicationContext)
        for (s in textInTable) {
            val pdfTextView = PDFTextView(applicationContext, PDFTextView.PDF_TEXT_SIZE.P)
            pdfTextView.setText("Row 1 : $s")
            tableRowView1.addToRow(pdfTextView)
        }
        val tableView = PDFTableView(applicationContext, tableHeader, tableRowView1)
        for (i in 0..7) {
            // Create 10 rows
            val tableRowView = PDFTableRowView(applicationContext)
            for (s in textInTable) {
                val pdfTextView = PDFTextView(applicationContext, PDFTextView.PDF_TEXT_SIZE.P)
                pdfTextView.setText("Row " + (i + 2) + ": " + s)
                tableRowView.addToRow(pdfTextView)
            }
            tableView.addRow(tableRowView)
        }
        tableView.setColumnWidth(*widthPercent)
        pdfBody.addView(tableView)

        val lineSeparatorView4 =
            PDFLineSeparatorView(applicationContext).setBackgroundColor(Color.BLACK)
        pdfBody.addView(lineSeparatorView4)
        val pdfIconLicenseView = PDFTextView(applicationContext, PDFTextView.PDF_TEXT_SIZE.H3)
        val icon8Link = HtmlCompat.fromHtml(
            "Bill Generated By: <a href='https://sukhtaraintltd.com'>https://sukhtaraintltd.com</a>",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        pdfIconLicenseView.view.text = icon8Link
        pdfBody.addView(pdfIconLicenseView)
        return pdfBody
    }

    override fun getFooterView(pageIndex: Int): PDFFooterView? {
        val footerView = PDFFooterView(applicationContext)
        val pdfTextViewPage = PDFTextView(applicationContext, PDFTextView.PDF_TEXT_SIZE.SMALL)
        pdfTextViewPage.setText(String.format(Locale.getDefault(), "Page: %d", pageIndex + 1))
        pdfTextViewPage.setLayout(
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 0f
            )
        )
        pdfTextViewPage.view.gravity = Gravity.CENTER_HORIZONTAL
        footerView.addView(pdfTextViewPage)
        return footerView
    }

    @Nullable
    override fun getWatermarkView(forPage: Int): PDFImageView? {
        val pdfImageView = PDFImageView(applicationContext)
        val childLayoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            200, Gravity.CENTER
        )
        pdfImageView.setLayout(childLayoutParams)
        pdfImageView.setImageResource(com.sukhtaraitint.ticketing_system.R.drawable.sit_logo)
        pdfImageView.setImageScale(ImageView.ScaleType.FIT_CENTER)
        pdfImageView.view.alpha = 0.3f
        return pdfImageView
    }

    override fun onNextClicked(savedPDFFile: File?) {
        val pdfUri = Uri.fromFile(savedPDFFile)
        val intentPdfViewer =
            Intent(this@BillGenerateActivity, PDFViewerActivity::class.java)
        intentPdfViewer.putExtra(PDFViewerActivity.PDF_FILE_URI, pdfUri)
        startActivity(intentPdfViewer)
    }
}