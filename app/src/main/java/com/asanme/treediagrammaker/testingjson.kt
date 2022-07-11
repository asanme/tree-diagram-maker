package com.asanme.treediagrammaker
import com.google.gson.Gson
import dev.bandb.graphview.graph.Graph
import dev.bandb.graphview.graph.Node
import java.io.FileReader
import java.util.*

class Nodes(
    val name: String,
    val children: List<Nodes>
){
    override fun toString(): String {
        return "\nName:\n$name\nChildren:\n$children\n"
    }

    fun hasChildren(): Boolean {
        return !children.isNullOrEmpty()
    }
}

private val pila: Deque<Nodes> = LinkedList()
lateinit var prevNode : Node
lateinit var graph: Graph

fun main() {
    val path = "C:\\Users\\asanme\\AndroidStudioProjects\\TreeDiagramMaker\\tree-diagram-maker\\app\\src\\main\\java\\com\\asanme\\treediagrammaker\\testing.json"
    val gson = Gson() // Or use new GsonBuilder().create();
    val tree: Nodes = gson.fromJson(FileReader(path), Nodes::class.java)
    graph = Graph()
    println("root : ${tree.name}")
    pila.push(tree)

    println("\nRoot Node: ${tree.name}\n")
    while(pila.isNotEmpty()){
        checkForChildren(pila.pop())
    }

    println(graph.edges)
}

fun checkForChildren(nodes: Nodes) {
    val newNode = nodes
    val node1 = Node(nodes.name)

    for(node in nodes.children){
        if(node.hasChildren()){
            graph.addEdge(Node(nodes.name), Node(node.name))
            println("    ${nodes.name} -> ${node.name}")
            pila.push(node)
        }
    }
}
