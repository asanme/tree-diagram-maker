package com.asanme.treediagrammaker

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class testingclass(
    @JsonProperty("name")
    val name: String?= null,
    @JsonProperty("children")
    val children: MutableList<testingclass>?= null
){
    fun addChildren(nodePosition: Int, newNode: testingclass){
        children?.add(nodePosition, newNode)
        println(children)
    }

    fun returnNodePosition(name:String) : Int {
        for(node in this.children!!){
            println(node.name)
            if(node.name == name){
                return children.indexOf(node)
            }
        }
        return -1
    }

    fun returnChildren(): List<testingclass>? {
        return this.children
    }

    override fun toString(): String {
        return "\nName:\n$name\nChildren:\n$children\n"
    }
}
