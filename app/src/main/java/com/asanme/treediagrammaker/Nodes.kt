package com.asanme.treediagrammaker

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * @param name represents the data displayed within a diagram
 * @param children represents the list of nodes within a Diagram Tree
 */
class Nodes(
    val name: String,
    val children: List<Nodes>
){
    override fun toString(): String {
        if(children != null){
            return "Name:$name\nChildren:[]"
        }
        return "\nName:$name\nChildren:[$children]"
    }

    fun returnChildren(): List<Nodes> {
            return this.children
    }

    fun hasChildren(): Boolean {
        return !children.isNullOrEmpty()
    }
}
