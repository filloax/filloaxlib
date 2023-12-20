package com.filloax.fxlib

enum class SetBlockFlag(val flag: Int) {
    /*
    Original Javadoc:
    Sets a block state into this world.Flags are as follows:
    1 will cause a block update.
    2 will send the change to clients.
    4 will prevent the block from being re-rendered.
    8 will force any re-renders to run on the main thread instead
    16 will prevent neighbor reactions (e.g. fences connecting, observers pulsing).
    32 will prevent neighbor reactions from spawning drops.
    64 will signify the block is being moved.
    Flags can be OR-e
    From LevelWriter
     */
    UPDATE(1),
    NOTIFY_CLIENTS(2),
    PREVENT_RERENDER(4),
    MAIN_THREAD_RERENDER(8),
    NO_NEIGHBOR_REACTIONS(16),
    NO_NEIGHBOR_REACTION_DROPS(32),
    MOVED(64),
    ;

    companion object {
        fun or(vararg flags: SetBlockFlag): Int {
            return flags.map(SetBlockFlag::flag).reduce { acc, i -> acc or i }
        }
    }
}