package com.asanme.treediagrammaker

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.asanme.treediagrammaker.databinding.ActivityGraphBinding
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.JsonElement
import dev.bandb.graphview.AbstractGraphAdapter
import dev.bandb.graphview.graph.Graph
import dev.bandb.graphview.graph.Node
import dev.bandb.graphview.layouts.tree.BuchheimWalkerConfiguration
import dev.bandb.graphview.layouts.tree.BuchheimWalkerLayoutManager
import dev.bandb.graphview.layouts.tree.TreeEdgeDecoration
import kotlinx.coroutines.android.awaitFrame
import java.util.*

//TODO Add server based JSON parser to load the graph into the TreeGraphActivity
//TODO Add capability to reload graph based on edited node
//TODO Save elements into a new Node array, and then swap the old one with the new one by replacing the old data with the new one
//TODO load json from db!
@SuppressLint("NotifyDataSetChanged")
/**
 * Main class used to generate diagrams
 * @author asanme
 */
class TreeDiagramActivity : AppCompatActivity() {
    private lateinit var fab: FloatingActionButton
    private lateinit var configBtn: FloatingActionButton
    private lateinit var editBtn: FloatingActionButton
    private lateinit var deleteBtn: FloatingActionButton
    private lateinit var addBtn: FloatingActionButton
    private lateinit var filterBtn: FloatingActionButton
    private lateinit var adapter: AbstractGraphAdapter<NodeViewHolder>
    private lateinit var recyclerView: RecyclerView
    private var currentNode: Node? = null
    private lateinit var graph: Graph
    private lateinit var newGraph: Graph
    private val pila: Deque<Nodes> = LinkedList()
    private val newStack: Deque<Nodes> = LinkedList()
    private var mNodesList = mutableListOf<Nodes>()
    private lateinit var filterList: List<Button>
    private lateinit var configList: List<FloatingActionButton>
    private lateinit var binding: ActivityGraphBinding
    private lateinit var builder: BuchheimWalkerConfiguration.Builder
    private var clickedFilter = false
    private var clickedConfig = false
    private lateinit var json: String
    private lateinit var viewRef: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        filterList = listOf(layout1Btn, layout2Btn, layout3Btn, layout4Btn)
        configList = listOf(editBtn, deleteBtn, addBtn)

        builder = BuchheimWalkerConfiguration.Builder()
            .setSiblingSeparation(100)
            .setLevelSeparation(100)
            .setSubtreeSeparation(100)

        newGraph = createGraph()
        recyclerView = findViewById(R.id.recycler)

        hideOnCreate()
        setLayoutManager()
        setEdgeDecoration()
        setupGraphView(graph)
        setupListeners(layout1Btn, layout2Btn, layout3Btn, layout4Btn)
    }

    /**
     * Method used to generate the default graph
     * @see Graph
     */
    private fun createGraph(): Graph {
        graph = Graph()

        json =
            "{  \"name\":\"A\",  \"children\":  [    {      \"name\":\"B\",      \"children\": [        {          \"name\":\"G\",          \"children\": [{}]        }      ]    },    {      \"name\":\"C\",      \"children\":      [        {          \"name\":\"D\",          \"children\":          [            {              \"name\":\"E\",              \"children\": [{}]            },            {              \"name\":\"F\",              \"children\": [{}]            }          ]        }      ]    }  ]}"
        val gson = Gson()
        val tree: Nodes = gson.fromJson(json, Nodes::class.java)
        pila.push(tree)
        while (pila.isNotEmpty()) {
            checkForChildren(pila.pop())
        }

        return graph
    }

    /**
     * Recursive method used to generate the graph with the JSON
     * @see Nodes
     * @see Graph
     */
    private fun checkForChildren(nodes: Nodes) {
        for (node in nodes.children) {
            if (node.hasChildren()) {
                graph.addEdge(Node(nodes.name), Node(node.name))
                pila.push(node)
            }
        }
    }

    /**
     * Method used to add a new Node to the current Graph
     * @param graph Graph to which the node will be added
     * @param name Data that will be displayed upon adding the Node
     * @see Graph
     */
    private fun createNode(name: String, graph: Graph) {
        val newNode = Node(name)
        if (currentNode != null) {
            graph.addEdge(currentNode!!, newNode)
        } else {
            graph.addNode(newNode)
        }

        adapter.notifyDataSetChanged()
    }

    /**
     * @param text Text that will replace the old data
     * Method used to edit the selected node
     * @see setupGraphView
     * @see replaceJson
     */
    private fun editNode(text: String) {
        setupGraphView(replaceJson(currentNode!!.data.toString(), text))
    }

    /**
     * Method used to delete the selected Node
     * @see Node
     * @see Graph
     */
    private fun deleteNode() {
        Log.i("NEWGRAPH INFO:::", "${newGraph.edges}")
        newGraph.removeNode(currentNode!!)
        currentNode = null
        adapter.notifyDataSetChanged()
        Toast.makeText(this, "Deleted node", Toast.LENGTH_SHORT).show()
        hideConfig()
        fab.hide()
    }

    /**
     * Method used to replace the current JSON with a new one by replacing the selected node with new data
     * @param newData new entered data
     * @param oldData data to be replaced
     * @see Gson
     * @see Deque
     */
    private fun replaceJson(oldData: String, newData: String): Graph {
        newGraph = Graph()
        newStack.clear()
        mNodesList.clear()

        val gson = Gson()
        var mappedNodes: Nodes = gson.fromJson(json, Nodes::class.java)

//        val nodePosition = mappedNodes.returnNodePosition("B")
//        println("workin? $nodePosition")
//
//        //WIP Working on being able to add new JSON Objects to the JSON string
        val mapper = ObjectMapper().registerKotlinModule()

//        val allNodes = state.returnChildren()

        //println("first node name::: ${allNodes?.get(2)!!.name}")

//        if (allNodes != null) {
//            for(children in allNodes){
//                if(children.name == oldData){
//                    println("adding node (?)")
//                }
//
//                println(children.name)
//            }
//        }

        //mNodesList.add(Nodes(graph.nodes[0].data.toString(), mNodesList))
        newStack.push(mappedNodes)
        while (newStack.isNotEmpty()) {
            replaceData(newStack.pop(), oldData, newData)
        }

        for(element in mNodesList){
            if(element.name == currentNode!!.data.toString()){
                println("Adding new node to ${mNodesList.indexOf(element)}")
                mNodesList.add(mNodesList.indexOf(element), Nodes(newData, mNodesList))
            }
        }

        for(item in mNodesList){
            println(item.children)
        }

        //TODO Fix exception
//        val newJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mNodesList)
//        println("json::: \n $newJson")

        json = json.replace(oldData, newData, ignoreCase = false)
        return newGraph
    }

    /**
     * Method used to replace the data passed within the Deque
     * @param nodes List of Nodes
     * @param newData new entered data
     * @param oldData data to be replaced
     * @see Nodes
     */
    private fun replaceData(nodes: Nodes, oldData: String, newData: String) {
        for (node in nodes.children) {
            if (node.hasChildren()) {
                //println(node.children)
                if (node.name == oldData) {
                    //println("REPLACING ${node.name} FOR ${newData}")
                    mNodesList.add(node)
                    newGraph.addEdge(Node(nodes.name), Node(newData))
                    newStack.push(Nodes(newData, node.children))
                    //jsonObject.put(node.name, nodes)
                } else {
                    mNodesList.add(node)
                    newGraph.addEdge(Node(nodes.name), Node(node.name))
                    newStack.push(node)
                }
            }
        }
    }

    /**
     * Method used to setup all the listeners required
     * @param layout1Btn changes the orientation to ORIENTATION_TOP_BOTTOM
     * @param layout2Btn changes the orientation to ORIENTATION_BOTTOM_TOP
     * @param layout3Btn changes the orientation to ORIENTATION_LEFT_RIGHT
     * @param layout4Btn changes the orientation to ORIENTATION_RIGHT_LEFT
     * @see buildLayoutOrientation
     * @see onFilterClicked
     * @see onConfigClicked
     */
    private fun setupListeners(
        layout1Btn: Button,
        layout2Btn: Button,
        layout3Btn: Button,
        layout4Btn: Button
    ) {
        fab = findViewById(R.id.configNode)

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

        filterBtn.setOnClickListener {
            onFilterClicked()
        }

        configBtn.setOnClickListener {
            onConfigClicked()
        }

        addBtn.setOnClickListener {
            val view = View.inflate(this@TreeDiagramActivity, R.layout.custom_dialog_add, null)
            val build = androidx.appcompat.app.AlertDialog.Builder(this@TreeDiagramActivity)
            build.setView(view)

            val dialog = build.create()
            val addNodeEditTextInput =
                view.findViewById<TextInputLayout>(R.id.addNodeTextInput)
            val addNodeEditText = view.findViewById<TextInputEditText>(R.id.addNodeEditText)

            dialog.show()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setCancelable(false)

            view.findViewById<Button>(R.id.cancelEditBtn).setOnClickListener {
                dialog.dismiss()
            }

            view.findViewById<Button>(R.id.editNodeBtn).setOnClickListener {
                if (addNodeEditText.text.toString() == "") {
                    addNodeEditTextInput.error = "El nom no pot estar en blanc"
                } else {
                    createNode(addNodeEditText.text.toString(), graph)
                    dialog.dismiss()
                }
            }
        }

        editBtn.setOnClickListener {
            if (currentNode != null) {
                val view = View.inflate(this@TreeDiagramActivity, R.layout.custom_dialog_edit, null)
                val build = androidx.appcompat.app.AlertDialog.Builder(this@TreeDiagramActivity)
                build.setView(view)

                val dialog = build.create()
                val editTextError = view.findViewById<TextInputLayout>(R.id.editNodeTextInput)
                val editText = view.findViewById<TextInputEditText>(R.id.editNodeEditText)

                dialog.show()
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.setCancelable(false)

                view.findViewById<Button>(R.id.cancelEditBtn).setOnClickListener {
                    dialog.dismiss()
                }

                view.findViewById<Button>(R.id.editNodeBtn).setOnClickListener {
                    if (editText.text.toString() == "") {
                        editTextError.error = "El nom no pot estar en blanc"
                    } else {
                        editNode(editText.text.toString())
                        dialog.dismiss()
                    }
                }
            }
        }

        deleteBtn.setOnClickListener {
            if (currentNode != null) {
                val view =
                    View.inflate(this@TreeDiagramActivity, R.layout.custom_dialog_delete, null)
                val build = androidx.appcompat.app.AlertDialog.Builder(this@TreeDiagramActivity)
                build.setView(view)

                val dialog = build.create()

                dialog.show()
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.setCancelable(false)

                view.findViewById<Button>(R.id.cancelDeleteBtn).setOnClickListener {
                    dialog.dismiss()
                }

                view.findViewById<Button>(R.id.deleteNodeBtn).setOnClickListener{
                    deleteNode()
                    dialog.dismiss()
                }
            }
        }
    }

    /**
     * Method used hide the filter and configuration buttons onCreate()
     */
    private fun hideOnCreate() {
        for (btn in filterList) {
            btn.visibility = View.GONE
        }

        for (btn in configList) {
            btn.visibility = View.GONE
        }
    }

    /**
     * Method used to change the orientation of the
     * @param orientation value of the orientation
     * @see BuchheimWalkerConfiguration
     */
    private fun buildLayoutOrientation(orientation: Int) {
        builder.setOrientation(orientation)
        recyclerView.layoutManager = BuchheimWalkerLayoutManager(this, builder.build())
        recyclerView.adapter = adapter
    }

    /**
     * Method used to setup the animations and visibility of filter buttons
     * @see setFilterAnimation
     */
    private fun onFilterClicked() {
        clickedFilter = !clickedFilter
        setFilterAnimation()
    }

    /**
     * Method used to setup the animations and visibility of configuration buttons
     * @see setConfigAnimation
     */
    private fun onConfigClicked() {
        clickedConfig = !clickedConfig
        setConfigAnimation()
    }

    /**
     * Method used to load the animation of the filter buttons
     * @see Animations
     */
    private fun setFilterAnimation() {
        if (clickedFilter) {
            for (btn in filterList) {
                btn.isClickable = true
                btn.visibility = View.VISIBLE
                btn.startAnimation(Animations(applicationContext).fromBottomFilter)
            }

            filterBtn.startAnimation(Animations(applicationContext).rotateFilterOpen)
        } else {
            for (btn in filterList) {
                btn.isClickable = false
                btn.visibility = View.GONE
                btn.startAnimation(Animations(applicationContext).toBottomFilter)
            }

            filterBtn.startAnimation(Animations(applicationContext).rotateFilterClose)
        }
    }

    /**
     * Method used to load the animation of the configuration buttons
     * @see Animations
     */
    private fun setConfigAnimation() {
        if (clickedConfig) {
            for (btn in configList) {
                btn.isEnabled = true
                btn.isClickable = true
                btn.visibility = View.VISIBLE
                btn.startAnimation(Animations(applicationContext).fromBottomSettings)
            }

            configBtn.startAnimation(Animations(applicationContext).rotateSettingsOpen)
        } else {
            for (btn in configList) {
                btn.isEnabled = false
                btn.isClickable = false
                btn.visibility = View.GONE
                btn.startAnimation(Animations(applicationContext).toBottomSettings)
            }

            configBtn.startAnimation(Animations(applicationContext).rotateSettingsClose)
        }
    }

    /**
     * Method used to hide the configuration buttons
     * @see Animations
     */
    private fun hideConfig() {
        for (btn in configList) {
            btn.startAnimation(Animations(applicationContext).toBottomSettings)
            btn.isEnabled = false
            btn.visibility = View.GONE
            btn.isClickable = false
        }

        clickedConfig = !clickedConfig
    }

    /**
     * Method used to load the default graph or even reload with a new one
     * @param graph Graph that represents the data to be displayed
     * @see Graph
     */
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

    private inner class NodeViewHolder constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var textView: TextView = itemView.findViewById(R.id.textView)

        init {
            viewRef = itemView
            itemView.setOnClickListener {
                if (!fab.isShown) {
                    fab.show()
                }

                currentNode = adapter.getNode(bindingAdapterPosition)
                overrideColor(viewRef, itemView)
                viewRef = itemView
            }
        }
    }

    /**
     * Method used to override the selected node color and return the old one to the default color
     * @param newNode represents the newly selected node
     * @param oldNode reference to the previous selected node
     */
    private fun overrideColor(oldNode: View, newNode: View) {
        oldNode.background =
            AppCompatResources.getDrawable(applicationContext, R.drawable.circular_box)
        newNode.background =
            AppCompatResources.getDrawable(applicationContext, R.drawable.selected_box)
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    /**
     * Method used to setup the BuchheimWalkerConfiguration and LayoutManager
     * @see BuchheimWalkerConfiguration
     * @see BuchheimWalkerLayoutManager
     */
    private fun setLayoutManager() {
        val configuration = BuchheimWalkerConfiguration.Builder()
            .setSiblingSeparation(100)
            .setLevelSeparation(100)
            .setSubtreeSeparation(100)
            .setOrientation(BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM)
            .build()
        recyclerView.layoutManager = BuchheimWalkerLayoutManager(this, configuration)
    }

    /**
     * Makes the graph display the information with tree shape
     * @see TreeEdgeDecoration
     */
    private fun setEdgeDecoration() {
        recyclerView.addItemDecoration(TreeEdgeDecoration())
    }
}