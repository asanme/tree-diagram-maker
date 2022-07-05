package com.asanme.treediagrammaker

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dev.bandb.graphview.AbstractGraphAdapter
import dev.bandb.graphview.graph.Graph
import dev.bandb.graphview.graph.Node
import java.util.*

abstract class TreeLoaderActivity : AppCompatActivity() {
    protected lateinit var recyclerView: RecyclerView
    protected lateinit var adapter: AbstractGraphAdapter<NodeViewHolder>
    private lateinit var fab: FloatingActionButton
    private var currentNode: Node? = null
    private var nodeCount = 1

    private lateinit var filterBtnList: List<Button>

    private lateinit var configBtn: FloatingActionButton
    private lateinit var editBtn: FloatingActionButton
    private lateinit var deleteBtn: FloatingActionButton
    private lateinit var addBtn: FloatingActionButton

    private var clicked = false

    private lateinit var filterBtn: FloatingActionButton

    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim) }

    protected abstract fun createGraph(): Graph
    protected abstract fun setLayoutManager()
    protected abstract fun setEdgeDecoration()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        filterBtn = findViewById(R.id.filterView)
        configBtn = findViewById(R.id.configNode)

        editBtn = findViewById(R.id.editNode)
        deleteBtn = findViewById(R.id.deleteNode)
        addBtn = findViewById(R.id.createNode)

        var layout1Btn = findViewById<Button>(R.id.lay1)
        var layout2Btn = findViewById<Button>(R.id.lay2)
        var layout3Btn = findViewById<Button>(R.id.lay3)
        var layout4Btn = findViewById<Button>(R.id.lay4)

        filterBtnList = Arrays.asList(layout1Btn, layout2Btn, layout3Btn, layout4Btn)

        for(btn in filterBtnList){
            btn.visibility = View.GONE
            btn.setOnClickListener {
                Log.i("DEBUG INFO:::", "SHEEEESH")
            }
        }

        val graph = createGraph()
        recyclerView = findViewById(R.id.recycler)
        setLayoutManager()
        setEdgeDecoration()
        setupGraphView(graph)
        setupFab(graph)


        filterBtn.setOnClickListener{
            Log.i("EFB Info:::", "Clicked state: $clicked" )
            onFloatingBtnClicked()
        }
    }

    private fun onFloatingBtnClicked(){
        clicked = !clicked
        Log.i("EFB Info:::", "Clicked state: $clicked" )
        setVisibility()
        setAnimation()
    }

    private fun setAnimation() {
        if(clicked){
            for(btn in filterBtnList){
                Log.i("ANIMATION INFO:::", "ANIMATING OPENING" )
                btn.startAnimation(fromBottom)
            }

            filterBtn.startAnimation(rotateOpen)
        } else {
            for(btn in filterBtnList){
                Log.i("ANIMATION INFO:::", "ANIMATING CLOSING" )
                btn.startAnimation(toBottom)
            }

            filterBtn.startAnimation(rotateClose)
        }
    }

    private fun setVisibility() {
        if(!clicked){
            for(btn in filterBtnList){
                btn.visibility = View.GONE
                btn.isClickable = false
            }
        } else {
            for(btn in filterBtnList){
                btn.visibility = View.VISIBLE
                btn.isClickable = true
            }
        }
    }

    private fun onSettingsClicked(){
        clicked = !clicked
        Log.i("EFB Info:::", "Clicked state: $clicked" )
        setAnimationSettings()
        setSettingsVisibility()
    }

    private fun setAnimationSettings() {
        if(clicked){
            for(btn in filterBtnList){
                Log.i("ANIMATION INFO:::", "ANIMATING OPENING" )
                btn.startAnimation(fromBottom)
            }

            filterBtn.startAnimation(rotateOpen)
        } else {
            for(btn in filterBtnList){
                Log.i("ANIMATION INFO:::", "ANIMATING CLOSING" )
                btn.startAnimation(toBottom)
            }

            filterBtn.startAnimation(rotateClose)
        }
    }

    private fun setSettingsVisibility() {
        if(!clicked){
            for(btn in filterBtnList){
                btn.visibility = View.GONE
            }
        } else {
            for(btn in filterBtnList){
                btn.visibility = View.VISIBLE
            }
        }
    }

    //TODO Add server based JSON parser to load the graph into the TreeGraphActivity

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

    /*private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val ab = supportActionBar
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
            ab.setDisplayHomeAsUpEnabled(true)
        }
    }*/

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
                Snackbar.make(itemView, "Clicked on " + adapter.getNodeData(bindingAdapterPosition)?.toString(),
                        Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    protected val nodeText: String
        get() = "Exemple " + nodeCount++
}