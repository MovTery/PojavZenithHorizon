package com.movtery.versionlist;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.R;

import java.util.ArrayList;
import java.util.List;

public class VersionListAdapter extends BaseAdapter {
    private final static int ICON_INSTALLED = R.drawable.ic_pojav_full;
    private final static int ICON_RELEASE = R.drawable.ic_minecraft;
    private final static int ICON_SNAPSHOT = R.drawable.ic_command_block;
    private final static int ICON_BETA = R.drawable.ic_old_cobblestone;
    private final static int ICON_ALPHA = R.drawable.ic_old_grass_block;

    private final LayoutInflater mInflater;
    private final Context context;
    private final List<String> mData = new ArrayList<>();
    private final VersionType versionType; //图标类型

    public VersionListAdapter(Context context, VersionType versionType) {
        mInflater = LayoutInflater.from(context);
        this.versionType = versionType;
        this.context = context;
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
            row = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);

        TextView view = (TextView) row;
        view.setText(getItem(position));

        Drawable icon;
        switch (versionType) { //根据版本类型来设置图标
            case RELEASE:
                icon = getIcon(ICON_RELEASE);
                break;
            case SNAPSHOT:
                icon = getIcon(ICON_SNAPSHOT);
                break;
            case BETA:
                icon = getIcon(ICON_BETA);
                break;
            case ALPHA:
                icon = getIcon(ICON_ALPHA);
                break;
            default:
                icon = getIcon(ICON_INSTALLED);
        }

        view.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        view.setCompoundDrawablePadding(20);
        return row;
    }

    private Drawable getIcon(int iconRes) {
        return PojavZHTools.getScaledIcon(this.context.getResources(), iconRes, 36, this.context);
    }
}
