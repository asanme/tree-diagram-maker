package com.asanme.treediagrammaker

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dev.bandb.graphview.AbstractGraphAdapter
import dev.bandb.graphview.graph.Graph
import dev.bandb.graphview.graph.Node
import dev.bandb.graphview.layouts.tree.BuchheimWalkerConfiguration
import dev.bandb.graphview.layouts.tree.BuchheimWalkerLayoutManager
import dev.bandb.graphview.layouts.tree.TreeEdgeDecoration
import java.util.*

abstract class TreeLoaderActivity : AppCompatActivity() {
    protected lateinit var recyclerView: RecyclerView
    protected lateinit var adapter: AbstractGraphAdapter<NodeViewHolder>
    private lateinit var fab: FloatingActionButton
    private var currentNode: Node? = null
    private var nodeCount = 1

    private lateinit var filterList: List<Button>
    private lateinit var configList: List<FloatingActionButton>

    private lateinit var configBtn: FloatingActionButton
    private lateinit var editBtn: FloatingActionButton
    private lateinit var deleteBtn: FloatingActionButton
    private lateinit var addBtn: FloatingActionButton

    private lateinit var builder : BuchheimWalkerConfiguration.Builder

    private var clickedFilter = false
    private var clickedConfig = false

    private lateinit var filterBtn: FloatingActionButton

    private val rotateFilterOpen: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim) }
    private val rotateFilterClose: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim) }
    private val fromBottomFilter: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim) }
    private val toBottomFilter: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim) }

    private val rotateSettingsOpen: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim) }
    private val rotateSettingsClose: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim) }
    private val fromBottomSettings: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim) }
    private val toBottomSettings: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        filterBtn = findViewById(R.id.filterView)
        configBtn = findViewById(R.id.configNode)

        val editBtn = findViewById<FloatingActionButton>(R.id.editNode)
        val deleteBtn = findViewById<FloatingActionButton>(R.id.deleteNode)
        val addBtn = findViewById<FloatingActionButton>(R.id.createNode)

        var layout1Btn = findViewById<Button>(R.id.lay1)
        var layout2Btn = findViewById<Button>(R.id.lay2)
        var layout3Btn = findViewById<Button>(R.id.lay3)
        var layout4Btn = findViewById<Button>(R.id.lay4)

        filterList = Arrays.asList(layout1Btn, layout2Btn, layout3Btn, layout4Btn)
        configList = Arrays.asList(editBtn, deleteBtn, addBtn)
        builder = BuchheimWalkerConfiguration.Builder()
            .setSiblingSeparation(100)
            .setLevelSeparation(300)
            .setSubtreeSeparation(300)

        val graph = createGraph()
        recyclerView = findViewById(R.id.recycler)
        setLayoutManager()
        setEdgeDecoration()
        setupGraphView(graph)
        setupFab(graph)

        layout1Btn.setOnClickListener {
            builder.setOrientation(BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM)
            recyclerView.layoutManager = BuchheimWalkerLayoutManager(this, builder.build())
            recyclerView.adapter = adapter
        }

        layout2Btn.setOnClickListener {
            builder.setOrientation(BuchheimWalkerConfiguration.ORIENTATION_BOTTOM_TOP)
            recyclerView.layoutManager = BuchheimWalkerLayoutManager(this, builder.build())
            recyclerView.adapter = adapter
        }

        layout3Btn.setOnClickListener {
            builder.setOrientation(BuchheimWalkerConfiguration.ORIENTATION_LEFT_RIGHT)
            recyclerView.layoutManager = BuchheimWalkerLayoutManager(this, builder.build())
            recyclerView.adapter = adapter
        }

        layout4Btn.setOnClickListener {
            builder.setOrientation(BuchheimWalkerConfiguration.ORIENTATION_RIGHT_LEFT)
            recyclerView.layoutManager = BuchheimWalkerLayoutManager(this, builder.build())
            recyclerView.adapter = adapter
        }

        for(btn in filterList){
            btn.visibility = View.GONE
        }

        for(btn in configList){
            btn.visibility = View.GONE
        }

        filterBtn.setOnClickListener{
            Log.i("EFB Info:::", "Clicked state: $clickedFilter" )
            onFilterClicked(filterList)
        }

        configBtn.setOnClickListener{
            Log.i("EFB Info:::", "Clicked state: $clickedFilter" )
            onConfigClicked()
        }
    }

    private fun onFilterClicked(buttonList : List<Button>){
        clickedFilter = !clickedFilter
        Log.i("EFB Info:::", "Clicked state: $clickedFilter" )
        setFilterVisibility()
        setFilterAnimation()
    }

    private fun setFilterAnimation() {
        if(clickedFilter){
            for(btn in filterList){
                Log.i("ANIMATION INFO:::", "ANIMATING OPENING" )
                btn.startAnimation(fromBottomFilter)
            }

            filterBtn.startAnimation(rotateFilterOpen)
        } else {
            for(btn in filterList){
                Log.i("ANIMATION INFO:::", "ANIMATING CLOSING" )
                btn.startAnimation(toBottomFilter)
            }

            filterBtn.startAnimation(rotateFilterClose)
        }
    }

    private fun setFilterVisibility() {
        if(!clickedFilter){
            for(btn in filterList){
                btn.visibility = View.GONE
                btn.isClickable = false
            }
        } else {
            for(btn in filterList){
                btn.visibility = View.VISIBLE
                btn.isClickable = true
            }
        }
    }


    private fun onConfigClicked(){
        clickedConfig = !clickedConfig
        Log.i("EFB Info:::", "Clicked state: $clickedConfig" )
        setConfigVisibility()
        setConfigAnimation()
    }

    private fun setConfigAnimation() {
        if(clickedConfig){
            for(btn in configList){
                Log.i("ANIMATION INFO:::", "ANIMATING OPENING" )
                btn.startAnimation(fromBottomSettings)
            }

            configBtn.startAnimation(rotateSettingsOpen)
        } else {
            for(btn in configList){
                Log.i("ANIMATION INFO:::", "ANIMATING CLOSING" )
                btn.startAnimation(toBottomSettings)
            }

            configBtn.startAnimation(rotateSettingsClose)
        }
    }

    private fun setConfigVisibility() {
        if(!clickedConfig){
            for(btn in configList){
                btn.visibility = View.GONE
                btn.isClickable = false
            }
        } else {
            for(btn in configList){
                btn.visibility = View.VISIBLE
                btn.isClickable = true
            }
        }
    }


    //TODO Add server based JSON parser to load the graph into the TreeGraphActivity
    //TODO Add capability to reload graph based on edited node (redraw)
    private fun setupGraphView(graph: Graph) {
        adapter = object : AbstractGraphAdapter<NodeViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NodeViewHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.node, parent, false)
                return NodeViewHolder(view)
            }

            override fun onBindViewHolder(holder: NodeViewHolder, position: Int) {
                holder.textView.text = Objects.requireNonNull(getNodeData(position)).toString()
            }
        }.apply {
            this.submitGraph(graph)
            recyclerView.adapter = this
        }
    }

    private fun setupFab(graph: Graph) {
        fab = findViewById(R.id.configNode)
        fab.setOnClickListener {
            val newNode = Node(nodeText)
            if (currentNode != null) {
                graph.addEdge(currentNode!!, newNode)
            } else {
                graph.addNode(newNode)
            }
            adapter.notifyDataSetChanged()
        }

        fab.setOnLongClickListener {
            if (currentNode != null) {
                graph.removeNode(currentNode!!)
                currentNode = null
                adapter.notifyDataSetChanged()
                fab.hide()
            }
            true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    protected inner class NodeViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView = itemView.findViewById(R.id.textView)

        init {
            itemView.setOnClickListener {

                if (!fab.isShown) {
                    fab.show()
                }
                currentNode = adapter.getNode(bindingAdapterPosition)
                Snackbar.make(itemView, "Clicked on " + adapter.getNodeData(bindingAdapterPosition)?.toString(), Snackbar.LENGTH_SHORT).show()

                Snackbar.make(itemView, "Node data: " + adapter.getNodeData(bindingAdapterPosition)?.toString(), Snackbar.LENGTH_SHORT).show()

            }
        }
    }

    private val nodeText: String
        get() = "Exemple " + nodeCount++


    private fun setLayoutManager() {
        val configuration = BuchheimWalkerConfiguration.Builder()
            .setSiblingSeparation(100)
            .setLevelSeparation(100)
            .setSubtreeSeparation(100)
            .setOrientation(BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM)
            .build()
        recyclerView.layoutManager = BuchheimWalkerLayoutManager(this, configuration)
    }

    private fun setEdgeDecoration() {
        recyclerView.addItemDecoration(TreeEdgeDecoration())
    }

    private fun createGraph(): Graph {
        val graph = Graph()
        return graph
    }

    //TODO recreate the graph based on the changed node (notify changes)
    private fun recreateGraph() : Graph {
        val newGraph = Graph()
        return newGraph
    }
}