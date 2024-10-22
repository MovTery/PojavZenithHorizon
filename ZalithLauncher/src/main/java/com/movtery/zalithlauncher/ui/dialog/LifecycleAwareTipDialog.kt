package com.movtery.zalithlauncher.ui.dialog

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import net.kdt.pojavlaunch.Tools
import java.util.concurrent.atomic.AtomicBoolean

abstract class LifecycleAwareTipDialog: LifecycleEventObserver {
    private var mLifecycle: Lifecycle? = null
    private var mDialog: TipDialog? = null
    private var mLifecycleEnded = false

    /**
     * Show the lifecycle-aware dialog.
     * Note that the DialogCreator may not be always invoked.
     * @param lifecycle the lifecycle to follow
     * Note that any dismiss listeners added to the dialog must be wrapped
     * with wrapDismissListener().
     */
    fun show(lifecycle: Lifecycle, builder: TipDialog.Builder) {
        this.mLifecycleEnded = false
        this.mLifecycle = lifecycle.apply {
            if (currentState == Lifecycle.State.DESTROYED) {
                mLifecycleEnded = true
                dialogHidden(true)
                return
            }
            builder.setDialogDismissListener {
                dispatchDialogHidden()
                true
            }
            addObserver(this@LifecycleAwareTipDialog)
        }
        mDialog = builder.buildDialog()
    }

    /**
     * Invoked when the dialog gets hidden either by cancel()/dismiss(), or if a lifecycle event
     * happens.
     * @param lifecycleEnded if the dialog was hidden due to a lifecycle event
     */
    protected abstract fun dialogHidden(lifecycleEnded: Boolean)

    private fun dispatchDialogHidden() {
        Exception().printStackTrace()
        dialogHidden(mLifecycleEnded)
        mLifecycle!!.removeObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            mDialog?.dismiss()
            mLifecycleEnded = true
        }
    }

    companion object {
        @JvmStatic
        fun haltOnDialog(lifecycle: Lifecycle, builder: TipDialog.Builder): Boolean {
            val waitLock = Object()
            val hasLifecycleEnded = AtomicBoolean(false)

            // This runnable is moved here in order to reduce bracket/lambda hell
            val showDialogRunnable = Runnable {
                val dialogBuilder: LifecycleAwareTipDialog =
                    object : LifecycleAwareTipDialog() {
                        override fun dialogHidden(lifecycleEnded: Boolean) {
                            hasLifecycleEnded.set(lifecycleEnded)
                            synchronized(waitLock) { waitLock.notifyAll() }
                        }
                    }
                dialogBuilder.show(lifecycle, builder)
            }
            synchronized(waitLock) {
                Tools.runOnUiThread(showDialogRunnable)
                // the wait() method makes the thread wait on the end of the synchronized block.
                // so we put it here to make sure that the thread won't get notified before wait()
                // is called
                waitLock.wait()
            }
            return hasLifecycleEnded.get()
        }
    }
}