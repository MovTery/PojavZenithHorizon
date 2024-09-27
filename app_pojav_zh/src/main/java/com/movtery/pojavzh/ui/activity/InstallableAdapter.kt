package com.movtery.pojavzh.ui.activity

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.movtery.pojavzh.feature.unpack.OnTaskRunningListener
import net.kdt.pojavlaunch.R

class InstallableAdapter(
    private val items: List<InstallableItem>,
    private val listener: TaskCompletionListener
) : RecyclerView.Adapter<InstallableAdapter.ViewHolder>() {
    @Volatile
    private var completedTasksCount = 0

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_installable, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun checkAllTask() {
        items.forEachIndexed { index, item ->
            if (!item.task.isNeedUnpack()) {
                item.isFinished = true
                updateTaskCount(index)
            }
        }
    }

    fun startAllTasks() {
        items.forEachIndexed { index, item ->
            if (!item.isFinished) {
                Thread {
                    item.task.apply {
                        setTaskRunningListener(object : OnTaskRunningListener {
                            override fun onTaskStart() {
                                item.isRunning = true
                                updateUI { notifyItemChanged(index) }
                            }

                            override fun onTaskEnd() {
                                item.isRunning = false
                                item.isFinished = true
                                updateTaskCount(index)
                            }
                        })
                    }
                    item.task.run()
                }.start()
            }
        }
    }

    @Synchronized
    private fun updateTaskCount(index: Int) {
        completedTasksCount++
        updateUI { notifyItemChanged(index) }

        if (completedTasksCount >= itemCount) {
            updateUI { listener.onAllTasksCompleted() }
        }
    }

    private fun updateUI(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post { action() }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var name: TextView = itemView.findViewById(R.id.name)
        private var summary: TextView = itemView.findViewById(R.id.summary)
        private var progress: View = itemView.findViewById(R.id.progress)
        private var finish: View = itemView.findViewById(R.id.finish)

        fun setData(item: InstallableItem) {
            name.text = item.name

            if (item.summary.isNullOrEmpty()) {
                summary.visibility = View.GONE
            } else {
                summary.text = item.summary
                summary.visibility = View.VISIBLE
            }

            progress.visibility = if (item.isRunning) View.VISIBLE else View.GONE
            finish.visibility = if (item.isFinished) View.VISIBLE else View.GONE
        }
    }

    fun interface TaskCompletionListener {
        fun onAllTasksCompleted()
    }
}
