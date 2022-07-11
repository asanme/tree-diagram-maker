package com.asanme.treediagrammaker

import android.app.AlertDialog
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import dev.bandb.graphview.AbstractGraphAdapter
import dev.bandb.graphview.graph.Graph
import dev.bandb.graphview.graph.Node
import dev.bandb.graphview.layouts.tree.BuchheimWalkerConfiguration
import dev.bandb.graphview.layouts.tree.BuchheimWalkerLayoutManager
import dev.bandb.graphview.layouts.tree.TreeEdgeDecoration
import java.io.FileReader
import java.util.*

abstract class TreeLoaderActivity : AppCompatActivity() {
    protected lateinit var recyclerView: RecyclerView
    protected lateinit var adapter: AbstractGraphAdapter<NodeViewHolder>
    private lateinit var fab: FloatingActionButton
    private var currentNode: Node? = null
    lateinit var graph : Graph

    private val pila: Deque<Nodes> = LinkedList()

    private var nodeCount = 1

    private lateinit var filterList: List<Button>
    private lateinit var configList: List<FloatingActionButton>

    private lateinit var configBtn: FloatingActionButton
    private lateinit var editBtn: FloatingActionButton
    private lateinit var deleteBtn: FloatingActionButton
    private lateinit var addBtn: FloatingActionButton

    private lateinit var builder : BuchheimWalkerConfiguration.Builder
    private lateinit var deleteAlert: AlertDialog.Builder
    private lateinit var editAlert: AlertDialog.Builder
    private lateinit var addAlert: AlertDialog.Builder

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

        deleteAlert = AlertDialog.Builder(this)
        editAlert = AlertDialog.Builder(this)
        addAlert = AlertDialog.Builder(this)

        filterBtn = findViewById(R.id.filterView)
        configBtn = findViewById(R.id.configNode)

        editBtn = findViewById(R.id.editNode)
        deleteBtn = findViewById(R.id.deleteNode)
        addBtn = findViewById(R.id.createNode)

        val layout1Btn = findViewById<Button>(R.id.lay1)
        val layout2Btn = findViewById<Button>(R.id.lay2)
        val layout3Btn = findViewById<Button>(R.id.lay3)
        val layout4Btn = findViewById<Button>(R.id.lay4)

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

    private fun onConfigClicked(){
        clickedConfig = !clickedConfig
        Log.i("EFB Info:::", "Clicked state: $clickedConfig" )
        setConfigVisibility()
        setConfigAnimation()
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
                btn.isEnabled = false
                btn.visibility = View.GONE
                btn.isClickable = false
            }
        } else {
            for(btn in configList){
                btn.isEnabled = true
                btn.visibility = View.VISIBLE
                btn.isClickable = true
            }
        }
    }

    private fun hideConfig(){
        for(btn in configList){
            btn.startAnimation(toBottomSettings)
            btn.isEnabled = false
            btn.visibility = View.GONE
            btn.isClickable = false
        }

        clickedConfig = !clickedConfig
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
        addBtn.setOnClickListener {
            generateNode(graph)
        }

        deleteBtn.setOnClickListener {
            if (currentNode != null) {
                generateWarning(graph)
            }
        }

        //TODO Save elements into a new Node array, and then swap the old one with
        // the new one by replacing the old data with the new one
        editBtn.setOnClickListener {
            if (currentNode != null) {
                generateEditNode(graph)
            }
        }
    }

    private fun generateNode(graph: Graph) {
        val name = EditText(this)
        var text = ""

        addAlert.setTitle("Nova etiqueta")
        addAlert.setMessage(
            HtmlCompat.fromHtml(
                "<b>Introdueix el nom de l'etiqueta</b>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        )

        addAlert.setView(name)

        addAlert.setPositiveButton(android.R.string.ok) { dialog, which ->
            if (name.text.toString() == "") {
                Toast.makeText(this, "El nom no pot estar buit", Toast.LENGTH_SHORT).show()
                generateNode(graph)
            } else {
                text = name.text.toString()
                createNode(text, graph)
            }
        }

        addAlert.setNegativeButton("CANCELAR") { dialog, which ->
            dialog.dismiss()
        }

        addAlert.show()
    }

    private fun generateEditNode(graph: Graph) {
        val name = EditText(this)
        var text = ""

        editAlert.setTitle("Editar etiqueta")
        editAlert.setMessage(
            HtmlCompat.fromHtml(
                "<b>Introdueix el nou nom</b>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        )

        editAlert.setView(name)

        editAlert.setPositiveButton(android.R.string.ok) { dialog, which ->
            if (name.text.toString() == "") {
                Toast.makeText(this, "El nom no pot estar buit", Toast.LENGTH_SHORT).show()
                generateEditNode(graph)
            } else {
                text = name.text.toString()
                editNode(text, graph)
            }
        }

        editAlert.setNegativeButton("CANCELAR") { dialog, which ->
            dialog.dismiss()
        }

        editAlert.show()
    }

    private fun generateWarning(graph: Graph) {
        deleteAlert.setTitle("Alerta")
        deleteAlert.setMessage(
            HtmlCompat.fromHtml(
                "Vols eliminar <b>${currentNode!!.data}</b>?",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        )

        deleteAlert.setPositiveButton(android.R.string.ok) { dialog, which ->
            deleteNode(graph)
        }

        deleteAlert.setNegativeButton("CANCELAR") { dialog, which ->
        }

        deleteAlert.show()
    }

    private fun createNode(name: String, graph: Graph) {
        val newNode = Node(name)
        if (currentNode != null) {
            graph.addEdge(currentNode!!, newNode)
        } else {
            graph.addNode(newNode)
        }

        adapter.notifyDataSetChanged()
    }

    private fun editNode(text:String, graph:Graph){
        //Check node to replace (x and y axis are unique to each node)
        for (node in graph.nodes) {
            if(node.x == currentNode!!.x && node.y == currentNode!!.y){
                node.data = text
            }
        }

        adapter.notifyDataSetChanged()
    }

    private fun deleteNode(graph: Graph) {
        graph.removeNode(currentNode!!)
        currentNode = null
        adapter.notifyDataSetChanged()
        hideConfig()
        fab.hide()
        Toast.makeText(this, "Deleted node", Toast.LENGTH_SHORT).show()
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
        graph = Graph()

        //val path = "C:\\Users\\asanme\\AndroidStudioProjects\\TreeDiagramMaker\\tree-diagram-maker\\app\\src\\main\\java\\com\\asanme\\treediagrammaker\\testing.json"
        val json = "{  \"name\":\"A\",  \"children\":  [    {      \"name\":\"B\",      \"children\": [        {          \"name\":\"G\",          \"children\": [{}]        }      ]    },    {      \"name\":\"C\",      \"children\":      [        {          \"name\":\"D\",          \"children\":          [            {              \"name\":\"E\",              \"children\": [{}]            },            {              \"name\":\"F\",              \"children\": [{}]            }          ]        }      ]    }  ]}"
        val gson = Gson() // Or use new GsonBuilder().create();
        val tree: Nodes = gson.fromJson(json, Nodes::class.java)
        graph = Graph()
        println("root : ${tree.name}")
        pila.push(tree)

        while(pila.isNotEmpty()){
            checkForChildren(pila.pop())
        }

        return graph
    }

    fun checkForChildren(nodes: Nodes) {
        for(node in nodes.children){
            if(node.hasChildren()){
                graph.addEdge(Node(nodes.name), Node(node.name))
                //println("    ${nodes.name} -> ${node.name}")
                pila.push(node)
            }
        }
    }
}