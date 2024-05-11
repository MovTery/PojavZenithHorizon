package com.movtery.versionlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import net.kdt.pojavlaunch.R;

import java.util.ArrayList;
import java.util.List;

import fr.spse.extended_view.ExtendedTextView;

public class VersionListAdapter extends BaseAdapter {
    private final static int ICON_INSTALLED = R.drawable.ic_pojav_full;
    private final static int ICON_RELEASE = R.drawable.ic_minecraft;
    private final static int ICON_SNAPSHOT = R.drawable.ic_command_block;
    private final static int ICON_BETA = R.drawable.ic_old_cobblestone;
    private final static int ICON_ALPHA = R.drawable.ic_old_grass_block;

    private final LayoutInflater mInflater;
    private final List<String> mData = new ArrayList<>();
    private final VersionType versionType; //图标类型

    public VersionListAdapter(Context context, VersionType versionType) {
        mInflater = LayoutInflater.from(context);
        this.versionType = versionType;
    }

    public void add(String versionId) {
        mData.add(versionId);
        notifyDataSetChanged();
    }

    @Override
    public String getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null)
            row = mInflater.inflate(R.layout.item_version_profile_layout, parent, false);

        ExtendedTextView view = (ExtendedTextView) row;
        view.setText(getItem(position));

        int icon;
        switch (versionType) { //根据版本类型来设置图标
            case RELEASE:
                icon = ICON_RELEASE;
                break;
            case SNAPSHOT:
                icon = ICON_SNAPSHOT;
                break;
            case BETA:
                icon = ICON_BETA;
                break;
            case ALPHA:
                icon = ICON_ALPHA;
                break;
            default:
                icon = ICON_INSTALLED;
        }

        view.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
        view.setCompoundDrawablePadding(20);
        return row;
    }
}
