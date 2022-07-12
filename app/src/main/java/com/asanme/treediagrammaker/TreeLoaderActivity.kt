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
import dev.bandb.graphview.graph.Edge
import dev.bandb.graphview.graph.Graph
import dev.bandb.graphview.graph.Node
import dev.bandb.graphview.layouts.tree.BuchheimWalkerConfiguration
import dev.bandb.graphview.layouts.tree.BuchheimWalkerLayoutManager
import dev.bandb.graphview.layouts.tree.TreeEdgeDecoration
import java.io.FileReader
import java.util.*
import com.amplifyframework.core.Amplify
import com.amplifyframework.AmplifyException
import com.asanme.treediagrammaker.databinding.ActivityGraphBinding
import kotlin.collections.ArrayList

abstract class TreeLoaderActivity : AppCompatActivity() {
    protected lateinit var recyclerView: RecyclerView
    protected lateinit var adapter: AbstractGraphAdapter<NodeViewHolder>
    private lateinit var fab: FloatingActionButton
    private var currentNode: Node? = null
    lateinit var graph : Graph

    private val pila: Deque<Nodes> = LinkedList()

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

    private lateinit var binding : ActivityGraphBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //View binding
        binding = ActivityGraphBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        filterBtn = binding.filterView
        configBtn = binding.configNode

        editBtn = binding.editNode
        deleteBtn = binding.deleteNode
        addBtn = binding.createNode

        val layout1Btn = binding.lay1
        val layout2Btn = binding.lay2
        val layout3Btn = binding.lay3
        val layout4Btn = binding.lay4

        filterList = Arrays.asList(layout1Btn, layout2Btn, layout3Btn, layout4Btn)
        configList = Arrays.asList(editBtn, deleteBtn, addBtn)
        builder = BuchheimWalkerConfiguration.Builder()
            .setSiblingSeparation(100)
            .setLevelSeparation(300)
            .setSubtreeSeparation(300)

        val graph = createGraph()
        recyclerView = findViewById(R.id.recycler)

        hideOnCreate()
        setupFab(graph)
        setLayoutManager()
        setEdgeDecoration()
        setupGraphView(graph)

        layout1Btn.setOnClickListener {
            buildLayoutOrientation(BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM)
        }

        layout2Btn.setOnClickListener {
            buildLayoutOrientation(BuchheimWalkerConfiguration.ORIENTATION_BOTTOM_TOP)
        }

        layout3Btn.setOnClickListener {
            buildLayoutOrientation(BuchheimWalkerConfiguration.ORIENTATION_LEFT_RIGHT)
        }

        layout4Btn.setOnClickListener {
            buildLayoutOrientation(BuchheimWalkerConfiguration.ORIENTATION_RIGHT_LEFT)
        }

        //Config buttons listeners
        filterBtn.setOnClickListener{
            onFilterClicked()
        }

        configBtn.setOnClickListener{
            onConfigClicked()
        }
    }

    private fun hideOnCreate() {
        //Hide buttons
        for (btn in filterList) {
            btn.visibility = View.GONE
        }

        for (btn in configList) {
            btn.visibility = View.GONE
        }
    }

    private fun buildLayoutOrientation(orientation: Int){
        builder.setOrientation(orientation)
        recyclerView.layoutManager = BuchheimWalkerLayoutManager(this, builder.build())
        recyclerView.adapter = adapter
    }

    private fun onFilterClicked(){
        clickedFilter = !clickedFilter
        setFilterVisibility()
        setFilterAnimation()
    }

    private fun onConfigClicked(){
        clickedConfig = !clickedConfig
        setConfigVisibility()
        setConfigAnimation()
    }

    private fun setFilterAnimation() {
        if(clickedFilter){
            for(btn in filterList){
                btn.startAnimation(fromBottomFilter)
            }

            filterBtn.startAnimation(rotateFilterOpen)
        } else {
            for(btn in filterList){
                btn.startAnimation(toBottomFilter)
            }

            filterBtn.startAnimation(rotateFilterClose)
        }
    }

    private fun setFilterVisibility() {
        if(!clickedFilter){
            for(btn in filterList){
                btn.isClickable = false
                btn.visibility = View.GONE
            }
        } else {
            for(btn in filterList){
                btn.isClickable = true
                btn.visibility = View.VISIBLE
            }
        }
    }

    private fun setConfigAnimation() {
        if(clickedConfig){
            for(btn in configList){
                btn.startAnimation(fromBottomSettings)
            }

            configBtn.startAnimation(rotateSettingsOpen)
        } else {
            for(btn in configList){
                btn.startAnimation(toBottomSettings)
            }

            configBtn.startAnimation(rotateSettingsClose)
        }
    }

    private fun setConfigVisibility() {
        if(!clickedConfig){
            for(btn in configList){
                btn.isEnabled = false
                btn.isClickable = false
                btn.visibility = View.GONE
            }
        } else {
            for(btn in configList){
                btn.isEnabled = true
                btn.isClickable = true
                btn.visibility = View.VISIBLE
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
            generateDialog("Nova etiqueta", "Introdueix el nom de l'etiqueta", "add")
        }

        //TODO Save elements into a new Node array, and then swap the old one with
        // the new one by replacing the old data with the new one
        editBtn.setOnClickListener {
            if (currentNode != null) {
                generateDialog("Editar etiqueta", "Introdueix el nou nom", "edit")
            }
        }

        deleteBtn.setOnClickListener {
            if (currentNode != null) {
                generateDialog("Alerta", "Vols eliminar ${currentNode!!.data}?", "delete")
            }
        }
    }

    private fun generateDialog(title:String, message:String, type:String){
        val newDialog = AlertDialog.Builder(this)

        when(type)
        {
            "add" ->
            {
                showDialog(newDialog, title, message, type,true)
            }

            "edit" ->
            {
                showDialog(newDialog, title, message, type,true)
            }

            "delete" ->
            {
                showDialog(newDialog, title, message, type, false)
            }
        }
    }

    private fun showDialog(newDialog: AlertDialog.Builder, title: String, message: String, type:String, isEditable: Boolean) {
        newDialog.setTitle(title)
        newDialog.setMessage(message)

        if(isEditable){
            val name = EditText(this)
            var text = ""
            newDialog.setView(name)
            newDialog.setPositiveButton(android.R.string.ok) { dialog, which ->
                if (name.text.toString() == "") {
                    Toast.makeText(this, "El nom no pot estar buit", Toast.LENGTH_SHORT).show()
                } else {
                    text = name.text.toString()
                    when(type){
                        "add" -> {
                            createNode(text, graph)
                        }

                        "edit" -> {
                            editNode(text, graph)
                        }
                    }
                    editNode(text, graph)
                }
            }
        } else {
            newDialog.setPositiveButton(android.R.string.ok) { dialog, which ->
                deleteNode(graph)
            }
        }

        newDialog.setNegativeButton("CANCELAR") { dialog, which ->

        }

        newDialog.show()
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
                pila.push(node)
            }
        }
    }
}