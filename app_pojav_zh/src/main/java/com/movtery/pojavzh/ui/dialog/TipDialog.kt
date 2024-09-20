package com.movtery.pojavzh.ui.dialog

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.movtery.pojavzh.ui.dialog.DraggableDialog.DialogInitializationListener
import net.kdt.pojavlaunch.R

class TipDialog private constructor(
    context: Context,
    private val title: String?,
    private val message: String?,
    private val confirm: String?,
    private val cancel: String?,
    private val moreView: Array<View>,
    private val showCancel: Boolean,
    private val showConfirm: Boolean,
    private val centerMessage: Boolean,
    private val cancelListener: OnCancelClickListener?,
    private val confirmListener: OnConfirmClickListener?,
    private val dismissListener: OnDialogDismissListener?
) : FullScreenDialog(context), DialogInitializationListener {
    init {
        init()
        DraggableDialog.initDialog(this)
    }

    private fun init() {
        setContentView(R.layout.dialog_tip)

        val titleView = findViewById<TextView>(R.id.zh_tip_title)
        val messageView = findViewById<TextView>(R.id.zh_tip_message)
        val moreViewLayout = findViewById<LinearLayoutCompat>(R.id.zh_tip_more)
        val cancelButton = findViewById<Button>(R.id.zh_tip_cancel)
        val confirmButton = findViewById<Button>(R.id.zh_tip_confirm)

        title?.apply { titleView.text = this }
        message?.apply { messageView.text = this }
        cancel?.apply { cancelButton.text = this }
        confirm?.apply { confirmButton.text = this }
        if (centerMessage) messageView.gravity = Gravity.CENTER_HORIZONTAL
        if (moreView.isNotEmpty()) {
            for (view in moreView) {
                moreViewLayout.addView(view)
            }
        }

        cancelButton.setOnClickListener {
            cancelListener?.onCancelClick()
            this.dismiss()
        }
        confirmButton.setOnClickListener {
            confirmListener?.onConfirmClick()
            this.dismiss()
        }

        cancelButton.visibility = if (showCancel) View.VISIBLE else View.GONE
        confirmButton.visibility = if (showConfirm) View.VISIBLE else View.GONE
    }

    override fun dismiss() {
        if (dismissListener?.onDismiss() == false) return
        super.dismiss()
    }

    override fun onInit(): Window {
        return window!!
    }

    fun interface OnCancelClickListener {
        fun onCancelClick()
    }

    fun interface OnConfirmClickListener {
        fun onConfirmClick()
    }

    fun interface OnDialogDismissListener {
        fun onDismiss(): Boolean
    }

    open class Builder(private val context: Context) {
        private val moreView: MutableList<View> = ArrayList()
        private var title: String? = null
        private var message: String? = null
        private var cancel: String? = null
        private var confirm: String? = null
        private var cancelClickListener: OnCancelClickListener? = null
        private var confirmClickListener: OnConfirmClickListener? = null
        private var dialogDismissListener: OnDialogDismissListener? = null
        private var cancelable = true
        private var showCancel = true
        private var showConfirm = true
        private var centerMessage = true

        fun buildDialog() {
            val tipDialog = TipDialog(
                this.context,
                title, message, confirm, cancel,
                moreView.toTypedArray<View>(),
                showCancel, showConfirm, centerMessage,
                cancelClickListener, confirmClickListener, dialogDismissListener
            )
            tipDialog.setCancelable(cancelable)
            tipDialog.show()
        }

        fun setTitle(title: String?): Builder {
            this.title = title
            return this
        }

        fun setTitle(title: Int): Builder {
            this.title = context.getString(title)
            return this
        }

        fun setMessage(message: String?): Builder {
            this.message = message
            return this
        }

        fun setMessage(message: Int): Builder {
            this.message = context.getString(message)
            return this
        }

        fun setCancel(cancel: String?): Builder {
            this.cancel = cancel
            return this
        }

        fun setCancel(cancel: Int): Builder {
            this.cancel = context.getString(cancel)
            return this
        }

        fun setConfirm(confirm: String?): Builder {
            this.confirm = confirm
            return this
        }

        fun setConfirm(confirm: Int): Builder {
            this.confirm = context.getString(confirm)
            return this
        }

        fun addView(view: View): Builder {
            moreView.add(view)
            return this
        }

        fun setCancelClickListener(cancelClickListener: OnCancelClickListener?): Builder {
            this.cancelClickListener = cancelClickListener
            return this
        }

        fun setConfirmClickListener(confirmClickListener: OnConfirmClickListener?): Builder {
            this.confirmClickListener = confirmClickListener
            return this
        }

        fun setDialogDismissListener(dialogDismissListener: OnDialogDismissListener?): Builder {
            this.dialogDismissListener = dialogDismissListener
            return this
        }

        fun setCancelable(cancelable: Boolean): Builder {
            this.cancelable = cancelable
            return this
        }

        fun setShowCancel(showCancel: Boolean): Builder {
            this.showCancel = showCancel
            return this
        }

        fun setShowConfirm(showConfirm: Boolean): Builder {
            this.showConfirm = showConfirm
            return this
        }

        fun setCenterMessage(center: Boolean): Builder {
            this.centerMessage = center
            return this
        }
    }
}
