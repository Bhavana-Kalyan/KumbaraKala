package com.kumbarakala

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class StoryCardActivity : AppCompatActivity() {

    private val API_KEY = "YOUR_API_KEY_HERE"

    private lateinit var tvProductName: TextView
    private lateinit var tvStoryText:   TextView
    private lateinit var btnGenerate:   Button
    private lateinit var btnShare:      Button
    private lateinit var progressBar:   ProgressBar
    private lateinit var cardPreview:   ImageView

    private var generatedStory = ""
    private var productName    = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_card)

        productName   = intent.getStringExtra("product_name") ?: "Clay Pot"
        tvProductName = findViewById(R.id.tvProductName)
        tvStoryText   = findViewById(R.id.tvStoryText)
        btnGenerate   = findViewById(R.id.btnGenerate)
        btnShare      = findViewById(R.id.btnShare)
        progressBar   = findViewById(R.id.progressBar)
        cardPreview   = findViewById(R.id.cardPreview)

        tvProductName.text  = productName
        btnShare.visibility = View.GONE

        btnGenerate.setOnClickListener {
            generateStoryFromGemini()
        }
    }

    private fun generateStoryFromGemini() {
        progressBar.visibility = View.VISIBLE
        btnGenerate.isEnabled  = false
        tvStoryText.text       = "Generating your story card..."

        val prefs   = getSharedPreferences("artisan", Context.MODE_PRIVATE)
        val name    = prefs.getString("name", "Artisan") ?: "Artisan"
        val village = prefs.getString("village", "Village") ?: "Village"

        val prompt = "You are a marketing expert for traditional Indian clay pottery. " +
                "Write a short 2 to 3 line benefit card for a $productName. " +
                "Include one health or science fact and one eco fact. " +
                "Make it inspiring and modern. " +
                "End with: Made by $name from $village with love. " +
                "Keep it under 60 words total."

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
        }

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$API_KEY")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    btnGenerate.isEnabled  = true
                    tvStoryText.text = "Network error. Please check internet and try again."
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    btnGenerate.isEnabled  = true
                    try {
                        val json = JSONObject(body)
                        val candidates = json.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val text = candidates.getJSONObject(0)
                                .optJSONObject("content")
                                ?.optJSONArray("parts")
                                ?.optJSONObject(0)
                                ?.optString("text", "No text generated")
                            generatedStory = text ?: "No story generated"
                            tvStoryText.text = generatedStory
                            renderCardOnCanvas()
                            btnShare.visibility = View.VISIBLE
                        } else {
                            // If API fails use backup story
                            val backupStories = mapOf(
                                "Curd Pot"    to "This clay Curd Pot naturally maintains pH balance — keeping your curd fresh and chemical-free. Clay is 100% biodegradable unlike plastic containers.",
                                "Water Pot"   to "This clay Water Pot cools water naturally through evaporation — no electricity needed! Clay is earth's original eco-friendly material.",
                                "Cooking Pot" to "Cooking in clay adds natural minerals to your food. Zero chemicals, zero plastic — just pure earth and fire.",
                                "Clay Lamp"   to "This clay lamp burns clean with no toxic fumes. Handcrafted from natural earth — biodegradable and beautiful.",
                                "Flower Pot"  to "Clay pots are naturally porous — allowing roots to breathe. The healthiest home for your plants!",
                                "Mud Cup"     to "Drinking chai from a clay cup adds natural minerals. 100% biodegradable — no plastic waste!",
                                "Pickle Jar"  to "Clay naturally preserves pickles for months with zero chemicals. The alkaline nature of clay balances acidity perfectly.",
                                "Clay Tawa"   to "Cooking on clay tawa adds natural iron to your food. Even heat distribution means less oil needed!",
                                "Oil Lamp"    to "Pure clay oil lamp crafted by hand from natural earth. Burns clean, lasts long, 100% biodegradable.",
                                "Wall Decor"  to "Handcrafted clay wall decor — each piece unique. Natural clay regulates humidity in your home.",
                                "Rice Pot"    to "Clay keeps rice naturally fresh and flavourful. The porous nature allows gentle breathing — no chemicals needed.",
                                "Planter"     to "Clay planters allow roots to breathe through porous walls. Healthier plants, zero plastic waste!"
                            )
                            val prefs2 = getSharedPreferences("artisan", Context.MODE_PRIVATE)
                            val n = prefs2.getString("name", "Artisan") ?: "Artisan"
                            val v = prefs2.getString("village", "Village") ?: "Village"
                            val backup = backupStories[productName] ?: "This beautiful clay product is handcrafted with love. Natural, eco-friendly and chemical-free."
                            generatedStory = "$backup\n\nMade by $n from $v with love. 🏺"
                            tvStoryText.text = generatedStory
                            renderCardOnCanvas()
                            btnShare.visibility = View.VISIBLE
                        }
                    } catch (e: Exception) {
                        tvStoryText.text = "Error: " + e.message
                    }
                }
            }
        })
    }

    private fun renderCardOnCanvas() {
        val prefs   = getSharedPreferences("artisan", Context.MODE_PRIVATE)
        val name    = prefs.getString("name", "Artisan") ?: "Artisan"
        val village = prefs.getString("village", "Village") ?: "Village"
        val phone   = prefs.getString("phone", "") ?: ""

        val bitmap = Bitmap.createBitmap(1080, 1080, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val gradient = LinearGradient(
            0f, 0f, 0f, 1080f,
            intArrayOf(
                Color.parseColor("#7A2E0E"),
                Color.parseColor("#B5451B"),
                Color.parseColor("#C4622D")
            ),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, 1080f, 1080f, Paint().apply { shader = gradient })

        canvas.drawRoundRect(60f, 120f, 1020f, 900f, 30f, 30f,
            Paint().apply { color = Color.argb(220, 253, 246, 238) })

        canvas.drawText(productName, 540f, 230f, Paint().apply {
            color       = Color.parseColor("#7A2E0E")
            textSize    = 66f
            typeface    = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign   = Paint.Align.CENTER
            isAntiAlias = true
        })

        canvas.drawLine(120f, 260f, 960f, 260f, Paint().apply {
            color       = Color.parseColor("#B5451B")
            strokeWidth = 3f
        })

        val storyPaint = Paint().apply {
            color       = Color.parseColor("#2C1A0E")
            textSize    = 36f
            typeface    = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        drawWrappedText(canvas, generatedStory, storyPaint, 120f, 330f, 840f, 48f)

        canvas.drawText(name, 540f, 780f, Paint().apply {
            color       = Color.parseColor("#B5451B")
            textSize    = 42f
            typeface    = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign   = Paint.Align.CENTER
            isAntiAlias = true
        })

        canvas.drawText("$village  |  $phone", 540f, 830f, Paint().apply {
            color       = Color.parseColor("#5C6B2E")
            textSize    = 32f
            typeface    = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            textAlign   = Paint.Align.CENTER
            isAntiAlias = true
        })

        canvas.drawText("Kumbara-Kala — State Pride", 540f, 980f, Paint().apply {
            color       = Color.WHITE
            textSize    = 28f
            textAlign   = Paint.Align.CENTER
            isAntiAlias = true
        })

        cardPreview.setImageBitmap(bitmap)
        saveBitmapToFile(bitmap)
    }

    private fun drawWrappedText(
        canvas: Canvas, text: String, paint: Paint,
        x: Float, startY: Float, maxWidth: Float, lineHeight: Float
    ) {
        val words = text.split(" ")
        var line  = ""
        var y     = startY
        for (word in words) {
            val test = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(test) > maxWidth) {
                canvas.drawText(line, x, y, paint)
                line = word
                y   += lineHeight
            } else {
                line = test
            }
        }
        if (line.isNotEmpty()) canvas.drawText(line, x, y, paint)
    }

    private fun saveBitmapToFile(bitmap: Bitmap) {
        val dir  = File(filesDir, "images")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "story_card.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
        val uri = FileProvider.getUriForFile(
            this, "com.kumbarakala.fileprovider", file)
        btnShare.setOnClickListener { shareToWhatsApp(uri) }
    }

    private fun shareToWhatsApp(uri: Uri) {
        val prefs = getSharedPreferences("artisan", Context.MODE_PRIVATE)
        val name  = prefs.getString("name", "Artisan") ?: "Artisan"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type    = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT,
                "Hello! I am $name. Check out my clay $productName. " +
                        "Made the traditional way with love!")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.whatsapp")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            intent.setPackage(null)
            startActivity(Intent.createChooser(intent, "Share via"))
        }
    }
}