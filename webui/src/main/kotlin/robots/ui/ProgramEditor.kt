package robots.ui

import dnd.draggable
import dnd.dropTarget
import kotlinx.html.DIV
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.RDOMBuilder
import react.dom.div
import robots.AST
import robots.Action
import robots.EditPoint
import robots.Repeat
import robots.Seq
import robots.children
import robots.editPoints
import robots.splitAfter


private interface CardProps : RProps {
    var editor: EditPoint
    var onEdit: (Seq) -> Unit
}

private class ExtensionSpace(props: ExtensionSpace.Props) : RComponent<ExtensionSpace.Props, RState>(props) {
    interface Props : CardProps
    
    override fun RBuilder.render() {
        dropTarget(::canAccept, ::accept) {
            div("cursor") {}
        }
    }
    
    private fun canAccept(dragged: Any) =
        dragged is AST
    
    private fun accept(dropped: Any) {
        val newProgram = props.editor.insertAfter(dropped as AST)
        props.onEdit(newProgram)
    }
}

fun RBuilder.extensionSpace(editor: EditPoint, onEdit: (Seq) -> Unit) = child(ExtensionSpace::class) {
    attrs.editor = editor
    attrs.onEdit = onEdit
}


private class ActionCard(props: CardProps) : RComponent<CardProps, RState>(props) {
    override fun RBuilder.render() {
        draggable(dataProvider = { props.editor }) {
            div("card action") { +props.editor.displayId() }
        }
    }
}

fun RBuilder.actionCard(editor: EditPoint) = child(ActionCard::class) {
    attrs.editor = editor
}

private class ControlCard(props: CardProps) : RComponent<CardProps, RState>(props) {
    override fun RBuilder.render() {
        div("card control") { +props.editor.displayId() }
    }
}

fun RBuilder.controlCard(editor: EditPoint) = child(ControlCard::class) {
    attrs.editor = editor
}

fun RDOMBuilder<DIV>.repeatBlock(editPoint: EditPoint, onEdit: (Seq) -> Unit) {
    div("cardblock") {
        controlCard(editPoint)
        cardSequence(editPoint.children(), onEdit)
    }
}

private class SequenceEditor(props: Props) : RComponent<SequenceEditor.Props, RState>(props) {
    interface Props : RProps {
        var elements: List<EditPoint>
        var onEdit: (Seq) -> Unit
    }
    
    override fun RBuilder.render() {
        div("cardsequence") {
            props.elements
                .splitAfter { it.node is Repeat }
                .forEach { row -> cardRow(row) }
        }
    }
    
    private fun RDOMBuilder<DIV>.cardRow(row: List<EditPoint>) {
        div("cardrow") {
            row.forEach { editPoint -> cardRowElement(editPoint) }
            
            // TODO - handle empty lists with a mandatory "add first element" cursor
            row.lastOrNull()?.let {
                if (it.node !is Repeat) {
                    extensionSpace(row.last(), props.onEdit)
                }
            }
        }
    }
    
    private fun RDOMBuilder<DIV>.cardRowElement(editPoint: EditPoint) {
        val node = editPoint.node
        when (node) {
            is Action -> actionCard(editPoint)
            is Repeat -> repeatBlock(editPoint, props.onEdit)
            else -> TODO()
        }
    }
}

fun RBuilder.cardSequence(elements: List<EditPoint>, onEdit: (Seq) -> Unit) = child(SequenceEditor::class) {
    attrs.elements = elements
    attrs.onEdit = onEdit
}

private class ProgramEditor(props: Props) : RComponent<ProgramEditor.Props, ProgramEditor.State>(props) {
    interface Props : RProps {
        var initialProgram: Seq
    }
    
    data class State(val program: Seq) : RState
    
    init {
        state = State(props.initialProgram)
    }
    
    override fun RBuilder.render() {
        cardSequence(state.program.editPoints(), onEdit = ::programEdited)
    }
    
    private fun programEdited(newProgramState: Seq) {
        setState({ State(program = newProgramState) })
    }
}


fun RBuilder.programEditor(edited: Seq) = child(ProgramEditor::class) {
    attrs.initialProgram = edited
}
