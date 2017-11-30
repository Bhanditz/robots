package robots

class EditPoint(
    val program: Seq,
    val path: ASTPath,
    val node: AST
) {
    operator fun contains(that: EditPoint) =
        this.path.contains(that.path)
    
    fun remove(): Seq =
        program.removeAt(path)
    fun replaceWith(newAST: AST): Seq =
        program.replaceAt(path, newAST)
    fun insertBefore(newAST: AST): Seq =
        program.insertBefore(path, newAST)
    fun insertAfter(newAST: AST): Seq =
        program.insertAfter(path, newAST)
    fun moveTo(destination: EditPoint, splice: Seq.(ASTPath, AST) -> Seq) =
        program.move(path, destination.path, splice)
}

fun Seq.editPoints(): List<EditPoint> =
    steps.mapIndexed { index, node -> EditPoint(this, pathOf(0, index), node) }

fun EditPoint.children(): List<EditPoint> {
    val node = this.node
    return when (node) {
        is Action -> emptyList()
        is Repeat -> this.childEditPoints(0, node.repeated)
        is Seq -> this.childEditPoints(0, node.steps)
    }
}

private fun EditPoint.childEditPoints(branchIndex: Int, children: PList<AST>): List<EditPoint> =
    children.mapIndexed { index, node -> EditPoint(program, path + ChildRef(branchIndex, index), node) }
