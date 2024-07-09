package net.kdt.pojavlaunch.multirt;

import static net.kdt.pojavlaunch.PojavApplication.sExecutorService;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.pojavzh.ui.dialog.SelectRuntimeDialog;
import com.movtery.pojavzh.ui.dialog.TipDialog;

import net.kdt.pojavlaunch.Architecture;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import java.io.IOException;
import java.util.List;

public class RTRecyclerViewAdapter extends RecyclerView.Adapter<RTRecyclerViewAdapter.RTViewHolder> {
    private SelectRuntimeDialog.RuntimeSelectedListener mSelectedListener;
    private boolean mIsSelectMode = false;

    private boolean mIsDeleting = false;

    public RTRecyclerViewAdapter() {
    }

    public RTRecyclerViewAdapter(SelectRuntimeDialog.RuntimeSelectedListener listener) {
        this.mIsSelectMode = true;
        this.mSelectedListener = listener;
    }

    @NonNull
    @Override
    public RTViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View recyclableView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_multirt_runtime,parent,false);
        return new RTViewHolder(recyclableView);
    }

    @Override
    public void onBindViewHolder(@NonNull RTViewHolder holder, int position) {
        final List<Runtime> runtimes = MultiRTUtils.getRuntimes();
        holder.bindRuntime(runtimes.get(position),position);
    }

    @Override
    public int getItemCount() {
        return MultiRTUtils.getRuntimes().size();
    }

    public boolean isDefaultRuntime(Runtime rt) {
        return LauncherPreferences.PREF_DEFAULT_RUNTIME.equals(rt.name);
    }

    @SuppressLint("NotifyDataSetChanged") //not a problem, given the typical size of the list
    public void setDefault(Runtime rt){
        LauncherPreferences.PREF_DEFAULT_RUNTIME = rt.name;
        LauncherPreferences.DEFAULT_PREF.edit().putString("defaultRuntime",LauncherPreferences.PREF_DEFAULT_RUNTIME).apply();
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged") //not a problem, given the typical size of the list
    public void setIsEditing(boolean isEditing) {
        mIsDeleting = isEditing;
        notifyDataSetChanged();
    }

    public boolean getIsEditing(){
        return mIsDeleting;
    }


    public class RTViewHolder extends RecyclerView.ViewHolder {
        final View mainItemView;
        final TextView mJavaVersionTextView;
        final TextView mFullJavaVersionTextView;
        final ColorStateList mDefaultColors;
        final Button mSetDefaultButton;
        final ImageButton mDeleteButton;
        final Context mContext;
        Runtime mCurrentRuntime;
        int mCurrentPosition;

        public RTViewHolder(View itemView) {
            super(itemView);
            mainItemView = itemView;
            mJavaVersionTextView = itemView.findViewById(R.id.multirt_view_java_version);
            mFullJavaVersionTextView = itemView.findViewById(R.id.multirt_view_java_version_full);
            mSetDefaultButton = itemView.findViewById(R.id.multirt_view_setdefaultbtn);
            mDeleteButton = itemView.findViewById(R.id.multirt_view_removebtn);

            mDefaultColors =  mFullJavaVersionTextView.getTextColors();
            mContext = itemView.getContext();

            setupOnClickListeners();
        }

        @SuppressLint("NotifyDataSetChanged") // same as all the other ones
        private void setupOnClickListeners() {
            mSetDefaultButton.setOnClickListener(v -> {
                if(mCurrentRuntime != null) {
                    setDefault(mCurrentRuntime);
                    RTRecyclerViewAdapter.this.notifyDataSetChanged();
                }
            });

            mDeleteButton.setOnClickListener(v -> {
                if (mCurrentRuntime == null) return;

                if(MultiRTUtils.getRuntimes().size() < 2) {
                    new TipDialog.Builder(mContext)
                            .setTitle(R.string.zh_warning)
                            .setMessage(R.string.multirt_config_removeerror_last)
                            .setShowCancel(false)
                            .buildDialog();
                    return;
                }

                sExecutorService.execute(() -> {
                    try {
                        MultiRTUtils.removeRuntimeNamed(mCurrentRuntime.name);
                        mDeleteButton.post(() -> {
                            if(getBindingAdapter() != null)
                                getBindingAdapter().notifyDataSetChanged();
                        });

                    } catch (IOException e) {
                        Tools.showError(itemView.getContext(), e);
                    }
                });

            });
        }

        public void bindRuntime(Runtime runtime, int pos) {
            if (mSelectedListener != null) mainItemView.setOnClickListener(v -> mSelectedListener.onSelected(runtime.name));

            mCurrentRuntime = runtime;
            mCurrentPosition = pos;
            if(runtime.versionString != null && Tools.DEVICE_ARCHITECTURE == Architecture.archAsInt(runtime.arch)) {
                mJavaVersionTextView.setText(runtime.name
                        .replace(".tar.xz", "")
                        .replace("-", " "));
                mFullJavaVersionTextView.setText(runtime.versionString);
                mFullJavaVersionTextView.setTextColor(mDefaultColors);

                updateButtonsVisibility();

                boolean defaultRuntime = isDefaultRuntime(runtime);
                mSetDefaultButton.setEnabled(!defaultRuntime);
                mSetDefaultButton.setText(defaultRuntime ? R.string.multirt_config_setdefault_already:R.string.multirt_config_setdefault);
                return;
            }

            // Problematic runtime moment, force propose deletion
            mDeleteButton.setVisibility(View.VISIBLE);
            if(runtime.versionString == null){
                mFullJavaVersionTextView.setText(R.string.multirt_runtime_corrupt);
            }else{
                mFullJavaVersionTextView.setText(mContext.getString(R.string.multirt_runtime_incompatiblearch, runtime.arch));
            }
            mJavaVersionTextView.setText(runtime.name);
            mFullJavaVersionTextView.setTextColor(Color.RED);
            mSetDefaultButton.setVisibility(View.GONE);
        }

        private void updateButtonsVisibility(){
            mSetDefaultButton.setVisibility(mIsSelectMode || mIsDeleting ? View.GONE : View.VISIBLE);
            mDeleteButton.setVisibility(mIsDeleting ? View.VISIBLE : View.GONE);
        }
    }
}
