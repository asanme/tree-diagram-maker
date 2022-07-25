package com.asanme.treediagrammaker

import com.fasterxml.jackson.annotation.JsonInclude
import kotlinx.serialization.Serializable

/**
 * @param name represents the data displayed within a diagram
 * @param children represents the list of nodes within a Diagram Tree
 */
@Serializable
class Nodes(
    var name: String,
    val children: MutableList<Nodes>
){
    override fun toString(): String {
        if(children != null){
            return "Name:$name\nChildren:[]"
        }
        return "\nName:$name\nChildren:[$children]"
    }

    fun addChildren(nodePosition: Int, newNode: Nodes){
        children.add(nodePosition, newNode)
        println(children)
    }

    fun returnNodePosition(name:String) : Int {
        for(node in this.children){
            println(node.name)
            if(node.name == name){
                return children.indexOf(node)
            }
        }

        return -1
    }

    fun returnChildren(): List<Nodes> {
            return this.children
    }

    fun hasChildren(): Boolean {
        return !children.isNullOrEmpty()
    }
}
