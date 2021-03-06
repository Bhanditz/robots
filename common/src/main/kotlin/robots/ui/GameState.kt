package robots.ui

import robots.Reduction
import robots.Seq
import robots.UndoRedoStack
import robots.canRedo
import robots.canUndo
import robots.cost
import robots.map
import robots.nop
import robots.redo
import robots.reduceToAction
import robots.undo


sealed class GameState {
    abstract val source: UndoRedoStack<Seq>
}

data class Editing(override val source: UndoRedoStack<Seq>) : GameState()
data class Running(override val source: UndoRedoStack<Seq>, val trace: UndoRedoStack<Reduction>) : GameState()

fun initialGameState() =
    Editing(UndoRedoStack(nop))

fun GameState.isRunning() =
    this is Running

fun GameState.startRunning() = when (this) {
    is Editing -> Running(source, UndoRedoStack(Reduction(nop, null, source.current)))
    is Running -> this
}

fun Running.hasFinished() =
    trace.current.next == nop

fun Running.stopRunning() =
    Editing(source)

fun Running.step(reduceFn: (Seq)->Reduction = Seq::reduceToAction): Running {
    return when (trace.current.next) {
        nop -> this
        else -> copy(trace = trace.map { reduceFn(it.next) })
    }
}

fun Running.canUndoStep() =
    trace.canUndo()

fun Running.undoStep() =
    copy(trace = trace.undo())

fun Running.canRedoStep() =
    trace.canRedo()

fun Running.redoStep() =
    copy(trace = trace.redo())

fun Running.saving(): Int =
    trace.cost() - source.current.cost()

fun UndoRedoStack<Reduction>.cost(): Int
    = current.price() + back.fold(0) { acc, r -> acc + r.price() }

private fun Reduction.price() =
    if (action != null) 1 else 0
