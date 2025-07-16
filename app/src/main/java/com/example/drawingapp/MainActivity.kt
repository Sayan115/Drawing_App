package com.example.drawingapp

import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.File
import java.io.FileOutputStream
import java.util.jar.Manifest
import kotlin.random.Random

class MainActivity : AppCompatActivity() , View.OnClickListener{
    private lateinit var drawingView: DrawingView
    private lateinit var brushButton: ImageButton
    private lateinit var purpleBtn: ImageButton
    private lateinit var redBtn: ImageButton
    private lateinit var orangeBtn: ImageButton
    private lateinit var greenBtn: ImageButton
    private lateinit var blueBtn: ImageButton
    private lateinit var yellowBtn: ImageButton
    private lateinit var pinkBtn: ImageButton
    private lateinit var blackBtn: ImageButton
    private lateinit var undoBtn: ImageButton
    private lateinit var saveBtn: ImageButton
    private var progress =0

    val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            permissions-> permissions.entries.forEach {
                val permissionName=it.key
            val isGranted=it.value
            if(isGranted&&permissionName== android.Manifest.permission.WRITE_EXTERNAL_STORAGE){
                Toast.makeText(this,"Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else{
                if(permissionName== android.Manifest.permission.WRITE_EXTERNAL_STORAGE){
                    Toast.makeText(this,"Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
        }
    private lateinit var colorPickerBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        brushButton=findViewById(R.id.brush_btn)
        drawingView=findViewById(R.id.drawingView)

        purpleBtn=findViewById(R.id.purple_btn)
        redBtn=findViewById(R.id.red_btn)
        orangeBtn=findViewById(R.id.orange_btn)
        greenBtn=findViewById(R.id.green_btn)
        blueBtn=findViewById(R.id.blue_btn)
        yellowBtn=findViewById(R.id.yellow_btn)
        pinkBtn=findViewById(R.id.pink_btn)
        blackBtn=findViewById(R.id.black_btn)

        saveBtn=findViewById(R.id.save_btn)
        colorPickerBtn=findViewById(R.id.palatte_btn)
        undoBtn=findViewById(R.id.undo_btn)

        drawingView.changeBrushSize(23.toFloat())
        brushButton.setOnClickListener {
            showBrushChooserDialog()
        }

        purpleBtn.setOnClickListener (this)
        redBtn.setOnClickListener (this)
        orangeBtn.setOnClickListener (this)
        greenBtn.setOnClickListener (this)
        blueBtn.setOnClickListener (this)
        yellowBtn.setOnClickListener (this)
        pinkBtn.setOnClickListener (this)
        blackBtn.setOnClickListener (this)

        undoBtn.setOnClickListener (this)
        colorPickerBtn.setOnClickListener (this)
        saveBtn.setOnClickListener (this)
    }

    private fun showBrushChooserDialog(){
        val brushDialog= Dialog(this@MainActivity)
        brushDialog.setContentView(R.layout.dialog_brush)
        val seekBarProgress=brushDialog.findViewById<SeekBar>(R.id.dialog_seekbar)
        val showProgress=brushDialog.findViewById<TextView>(R.id.dialog_textviewProgress)
        if(progress==0)
            showProgress.text="Choose brush thickness"
        else{
            showProgress.text=""+progress
            seekBarProgress.setProgress(progress)
        }
        seekBarProgress.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(
                seekBar: SeekBar,
                p1: Int,
                p2: Boolean
            ) {
                drawingView.changeBrushSize(seekBar.progress.toFloat())
                showProgress.text=seekBarProgress.progress.toString()
                progress=seekBarProgress.progress
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }

        })
        brushDialog.show()
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.purple_btn ->{
                drawingView.setColor("#8E24AA")
            }
            R.id.red_btn ->{
                drawingView.setColor("#FF0000")
            }
            R.id.orange_btn ->{
                drawingView.setColor("#FB8C00")
            }
            R.id.green_btn ->{
                drawingView.setColor("#00FF0C")
            }
            R.id.blue_btn ->{
                drawingView.setColor("#0227F0")
            }
            R.id.yellow_btn ->{
                drawingView.setColor("#FFD000")
            }
            R.id.pink_btn ->{
                drawingView.setColor("#FF8DA1")
            }
            R.id.black_btn ->{
                drawingView.setColor("#000000")
            }

            R.id.undo_btn ->{
                drawingView.undoPath()
            }
            R.id.palatte_btn ->{
                showColorPickerDialog()
            }
            R.id.save_btn ->{
                if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED){
                    requestStoragePermission()
                    //TODO: requestStoragePermission()
                    //Toast.makeText(this,"Permission not granted", Toast.LENGTH_SHORT).show()
                }else{
                    val layout=findViewById<DrawingView>(R.id.drawingView)
                    val bitmap=getBitmapfromView(layout)
                    CoroutineScope(IO).launch{
                        saveImg(bitmap)
                    }
                }
            }
        }
    }

    private fun showColorPickerDialog(){
        val dialog= AmbilWarnaDialog(this, Color.BLACK,object : AmbilWarnaDialog.OnAmbilWarnaListener{
            override fun onCancel(dialog: AmbilWarnaDialog?) {

            }

            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                drawingView.setColor(color)
            }

        })
        dialog.show()
    }

    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            Toast.makeText(this,"Storage access needed to store image",Toast.LENGTH_LONG).show()
        }
        else{
            requestPermission.launch(
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
        }
    }

    private fun getBitmapfromView(view:View): Bitmap{
        val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        val canvas= Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private suspend fun saveImg(bitmap: Bitmap){
        val root= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
        val myDir= File("$root/drawing_app")
        myDir.mkdirs()
        val generator= java.util.Random()
        var n=10000
        n=generator.nextInt(n)
        val outputFile=File(myDir,"Image_$n.png")
        if(outputFile.exists()){
            outputFile.delete()
        }
        else{
            try {
                val out= FileOutputStream(outputFile)
                bitmap.compress(Bitmap.CompressFormat.PNG,100,out)
                out.flush()
                out.close()

                // This is the important part to make it show up in Gallery
                MediaScannerConnection.scanFile(
                    this@MainActivity,
                    arrayOf(outputFile.absolutePath),
                    arrayOf("image/png"),
                    null
                )
            }catch (e: Exception){
                e.printStackTrace()
            }
            withContext(Main){
                Toast.makeText(this@MainActivity,"${outputFile.absolutePath} saved!",Toast.LENGTH_SHORT).show()
            }
        }

    }
}