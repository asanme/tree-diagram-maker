package com.asanme.treediagrammaker

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class testingclass(
    @JsonProperty("name")
    val name: String?= null,
    @JsonProperty("children")
    val children: List<testingclass>?= null
){
    fun returnChildren(): List<testingclass>? {
        return this.children
    }

    override fun toString(): String {
        return "\nName:\n$name\nChildren:\n$children\n"
    }
}
