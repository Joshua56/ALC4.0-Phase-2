package site.josh.alc40phase.sec.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.josh.alc40phase.sec.R;
import site.josh.alc40phase.sec.model.Deal;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class DealsRecyclerAdapter extends RecyclerView.Adapter<DealsRecyclerAdapter.ViewHolder> {

//    final List<Deal> deals = new LinkedList<>();

    static DiffUtil.ItemCallback DiffCallback = new DiffUtil.ItemCallback<Deal>() {
        @Override
        public boolean areItemsTheSame(@NonNull Deal oldItem, @NonNull Deal newItem) {
            return oldItem.uuid.equals(newItem.uuid);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Deal oldItem, @NonNull Deal newItem) {
            return oldItem.equals(newItem);
        }
    };

    AsyncListDiffer<Deal> differ = new AsyncListDiffer<>(this, DiffCallback);

    public DealsRecyclerAdapter() {
        differ.submitList(new ArrayList<Deal>());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_deal_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Deal deal = (Deal)differ.getCurrentList().get(position);
        holder.apply(deal);
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void apply(@NonNull final List<Deal> deals) {
        differ.submitList(deals);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        WeakReference<Context> weakContext;
        ImageView imageView;
        TextView title, description, amount;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            weakContext = new WeakReference<>(itemView.getContext());
            imageView = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.text_title);
            description = itemView.findViewById(R.id.text_description);
            amount = itemView.findViewById(R.id.text_amount);
        }

        public void apply(@NonNull final Deal deal) {
            title.setText(deal.name);
            description.setText(deal.description);
            amount.setText(deal.amount);

            Glide.with(weakContext.get())
                    .load(deal.imageUri)
                    .centerCrop()
                    .into(imageView);
        }
    }
}
