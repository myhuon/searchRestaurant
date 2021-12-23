package ddwucom.mobile.searchrestaurant;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class MyCursorAdapter extends CursorAdapter {
    LayoutInflater inflater;
    int layout;

    public MyCursorAdapter(Context context, int layout, Cursor c) {
        super(context, c, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layout = layout;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = inflater.inflate(layout, parent, false);
        ViewHolder holder = new ViewHolder();
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        if (holder.cbAreaName == null) {
            holder.cbAreaName = view.findViewById(R.id.cbName);
            holder.tvAreaPhone = view.findViewById(R.id.tvPhone_heart);
            holder.tvAreaAddress = view.findViewById(R.id.tvAddress_heart);
        }

        holder.cbAreaName.setText(cursor.getString(cursor.getColumnIndex(AreaDBHelper.COL_NAME)));
        holder.tvAreaPhone.setText(cursor.getString(cursor.getColumnIndex(AreaDBHelper.COL_PHONE)));
        holder.tvAreaAddress.setText(cursor.getString(cursor.getColumnIndex(AreaDBHelper.COL_ADDRESS)));
    }

    static class ViewHolder {

        public ViewHolder() {
            cbAreaName = null;
            tvAreaPhone = null;
            tvAreaAddress = null;
        }

        CheckBox cbAreaName;
        TextView tvAreaPhone;
        TextView tvAreaAddress;
    }
}
