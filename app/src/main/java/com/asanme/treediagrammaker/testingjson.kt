package com.asanme.treediagrammaker
import com.google.gson.Gson
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

fun main() {
    val path = "C:\\Users\\asanme\\AndroidStudioProjects\\TreeDiagramMaker\\tree-diagram-maker\\app\\src\\main\\java\\com\\asanme\\treediagrammaker\\testing.json"

    val gson = Gson() // Or use new GsonBuilder().create();

    println("strict digraph tree {")
    val tree: Nodes = gson.fromJson(FileReader(path), Nodes::class.java)
    pila.push(tree)
    while(pila.isNotEmpty()){
        checkForTesting(pila.pop())
    }
    println("}")

    //checkForChildren(tree, tree.name)
}

fun checkForChildren(nodes: Nodes, previousNode: String){
    //println(nodes)
    for(node in nodes.children){
        println("$previousNode -> ${node.name}")
        if(node.hasChildren()){
            checkForChildren(node, node.name)
        }
    }
}

fun checkForTesting(nodes: Nodes){
    //println(nodes)
    for(node in nodes.children){
        if(node.hasChildren()){
            println("    ${nodes.name} -> ${node.name}")
            pila.push(node)
        }
    }
}
