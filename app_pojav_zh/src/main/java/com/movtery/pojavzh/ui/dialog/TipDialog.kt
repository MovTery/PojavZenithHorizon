package com.movtery.pojavzh.ui.dialog

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.Window
import com.movtery.pojavzh.ui.dialog.DraggableDialog.DialogInitializationListener
import net.kdt.pojavlaunch.databinding.DialogTipBinding

class TipDialog private constructor(
    context: Context,
    title: String?,
    message: String?,
    confirm: String?,
    cancel: String?,
    moreView: Array<View>,
    showCancel: Boolean,
    showConfirm: Boolean,
    centerMessage: Boolean,
    private val cancelListener: OnCancelClickListener?,
    private val confirmListener: OnConfirmClickListener?,
    private val dismissListener: OnDialogDismissListener?
) : FullScreenDialog(context), DialogInitializationListener {
    private val binding = DialogTipBinding.inflate(layoutInflater)

    init {
        setContentView(binding.root)
        DraggableDialog.initDialog(this)

        title?.apply { binding.titleView.text = this }
        message?.apply { binding.messageView.text = this }
        cancel?.apply { binding.cancelButton.text = this }
        confirm?.apply { binding.confirmButton.text = this }
        if (centerMessage) binding.messageView.gravity = Gravity.CENTER_HORIZONTAL
        if (moreView.isNotEmpty()) {
            for (view in moreView) {
                binding.moreView.addView(view)
            }
        }

        binding.cancelButton.setOnClickListener {
            cancelListener?.onCancelClick()
            this.dismiss()
        }
        binding.confirmButton.setOnClickListener {
            confirmListener?.onConfirmClick()
            this.dismiss()
        }

        binding.cancelButton.visibility = if (showCancel) View.VISIBLE else View.GONE
        binding.confirmButton.visibility = if (showConfirm) View.VISIBLE else View.GONE
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

        fun buildDialog(): TipDialog {
            val tipDialog = TipDialog(
                this.context,
                title, message, confirm, cancel,
                moreView.toTypedArray<View>(),
                showCancel, showConfirm, centerMessage,
                cancelClickListener, confirmClickListener, dialogDismissListener
            )
            tipDialog.setCancelable(cancelable)
            tipDialog.show()
            return tipDialog
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
