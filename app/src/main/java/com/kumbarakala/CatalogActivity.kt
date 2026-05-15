package com.kumbarakala

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class ClayProduct(
    val name: String,
    val category: String,
    val benefit: String
)

class CatalogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        val products = listOf(
            ClayProduct("Curd Pot",    "Utensils",    "Maintains natural pH balance"),
            ClayProduct("Water Pot",   "Utensils",    "Naturally cools water"),
            ClayProduct("Cooking Pot", "Utensils",    "Non-toxic chemical-free cooking"),
            ClayProduct("Clay Lamp",   "Lamps",       "Eco-friendly traditional lamp"),
            ClayProduct("Flower Pot",  "Decoratives", "Breathable pot for healthy plants"),
            ClayProduct("Mud Cup",     "Utensils",    "Biodegradable earthy experience"),
            ClayProduct("Pickle Jar",  "Utensils",    "Preserves naturally for months"),
            ClayProduct("Clay Tawa",   "Utensils",    "Iron-rich cooking surface"),
            ClayProduct("Oil Lamp",    "Lamps",       "Pure clay pooja lamp"),
            ClayProduct("Wall Decor",  "Decoratives", "Handcrafted traditional art"),
            ClayProduct("Rice Pot",    "Utensils",    "Keeps rice fresh and flavoured"),
            ClayProduct("Planter",     "Decoratives", "Healthy root growth guaranteed")
        )

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerCatalog)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = CatalogAdapter(products) { product ->
            val intent = Intent(this, StoryCardActivity::class.java)
            intent.putExtra("product_name", product.name)
            intent.putExtra("product_benefit", product.benefit)
            startActivity(intent)
        }
    }
}

class CatalogAdapter(
    private val items: List<ClayProduct>,
    private val onClick: (ClayProduct) -> Unit
) : RecyclerView.Adapter<CatalogAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName:     TextView = view.findViewById(R.id.tvProductName)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvBenefit:  TextView = view.findViewById(R.id.tvBenefit)
        val btnGenerate: Button  = view.findViewById(R.id.btnGenerate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text     = item.name
        holder.tvCategory.text = item.category
        holder.tvBenefit.text  = item.benefit
        holder.btnGenerate.setOnClickListener { onClick(item) }
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}