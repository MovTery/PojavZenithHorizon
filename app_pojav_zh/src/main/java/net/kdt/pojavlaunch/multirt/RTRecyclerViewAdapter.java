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
import java.util.Objects;

public class RTRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<Runtime> mData;
    private SelectRuntimeDialog.RuntimeSelectedListener mSelectedListener;
    private final int TYPE_MODE_SELECT = 0;
    private final int TYPE_MODE_EDIT = 1;
    private int mType = TYPE_MODE_EDIT;
    private boolean mIsDeleting = false;

    public RTRecyclerViewAdapter(List<Runtime> mData) {
        this.mData = mData;
    }

    public RTRecyclerViewAdapter(List<Runtime> mData, SelectRuntimeDialog.RuntimeSelectedListener listener) {
        this.mData = mData;
        this.mType = TYPE_MODE_SELECT;
        this.mSelectedListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_MODE_SELECT:
                View recyclableView1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_multirt_runtime, parent,false);
                return new RTSelectViewHolder(recyclableView1);
            case TYPE_MODE_EDIT:
            default:
                View recyclableView2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_multirt_runtime,parent,false);
                return new RTEditViewHolder(recyclableView2);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_MODE_EDIT) {
            ((RTEditViewHolder) holder).bindRuntime(mData.get(position), position);
        } else {
            ((RTSelectViewHolder) holder).bindRuntime(mData.get(position));
        }
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    public boolean isDefaultRuntime(Runtime rt) {
        return LauncherPreferences.PREF_DEFAULT_RUNTIME.equals(rt.name);
    }

    @Override
    public int getItemViewType(int position) {
        switch (mType) {
            case TYPE_MODE_SELECT:
                return TYPE_MODE_SELECT;
            case TYPE_MODE_EDIT:
            default:
                return TYPE_MODE_EDIT;
        }
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

    private String getJavaVersionName(Runtime runtime) {
        return runtime.name.replace(".tar.xz", "")
                .replace("-", " ");
    }

    public class RTSelectViewHolder extends RecyclerView.ViewHolder {
        final View mainView;
        final Context mContext;
        final TextView mJavaVersionTextView;
        final TextView mFullJavaVersionTextView;

        public RTSelectViewHolder(@NonNull View itemView) {
            super(itemView);
            mainView = itemView;
            mContext = itemView.getContext();
            mJavaVersionTextView = itemView.findViewById(R.id.multirt_view_java_version);
            mFullJavaVersionTextView = itemView.findViewById(R.id.multirt_view_java_version_full);
        }

        public void bindRuntime(Runtime runtime) {
            if (!Objects.equals(runtime.name, "auto")) {
                if(runtime.versionString != null && Tools.DEVICE_ARCHITECTURE == Architecture.archAsInt(runtime.arch)) {
                    mJavaVersionTextView.setText(getJavaVersionName(runtime));
                    mFullJavaVersionTextView.setText(runtime.versionString);

                    mainView.setOnClickListener(v -> mSelectedListener.onSelected(runtime.name));
                    return;
                }

                if (runtime.versionString == null) {
                    mFullJavaVersionTextView.setText(R.string.multirt_runtime_corrupt);
                } else {
                    mFullJavaVersionTextView.setText(mContext.getString(R.string.multirt_runtime_incompatiblearch, runtime.arch));
                }
                mJavaVersionTextView.setText(runtime.name);
                mFullJavaVersionTextView.setTextColor(Color.RED);
            } else {
                //自动选择
                mJavaVersionTextView.setText(R.string.zh_install_auto_select);
                mFullJavaVersionTextView.setVisibility(View.GONE);
                mainView.setOnClickListener(v -> mSelectedListener.onSelected(null));
            }
        }
    }

    public class RTEditViewHolder extends RecyclerView.ViewHolder {
        final TextView mJavaVersionTextView;
        final TextView mFullJavaVersionTextView;
        final ColorStateList mDefaultColors;
        final Button mSetDefaultButton;
        final ImageButton mDeleteButton;
        final Context mContext;
        Runtime mCurrentRuntime;
        int mCurrentPosition;

        public RTEditViewHolder(View itemView) {
            super(itemView);
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
            mCurrentRuntime = runtime;
            mCurrentPosition = pos;
            if(runtime.versionString != null && Tools.DEVICE_ARCHITECTURE == Architecture.archAsInt(runtime.arch)) {
                mJavaVersionTextView.setText(getJavaVersionName(runtime));
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
            mSetDefaultButton.setVisibility(mIsDeleting ? View.GONE : View.VISIBLE);
            mDeleteButton.setVisibility(mIsDeleting ? View.VISIBLE : View.GONE);
        }
    }
}
