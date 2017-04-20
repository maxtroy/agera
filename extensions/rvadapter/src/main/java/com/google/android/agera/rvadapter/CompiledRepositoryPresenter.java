package com.google.android.agera.rvadapter;

import static java.util.Collections.emptyList;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.google.android.agera.Binder;
import com.google.android.agera.Function;
import com.google.android.agera.Receiver;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Implementation detail. Do not use directly.
 */
public class CompiledRepositoryPresenter extends RepositoryPresenter {
  @NonNull
  private final Function<Object, List<Object>> converter;
  @NonNull
  private final Binder<Object, View> collectionBinder;
  @NonNull
  private final Function<Object, Integer> layoutId;
  @NonNull
  private final Binder<Object, View> binder;
  @NonNull
  private final Function<Object, Long> stableIdForItem;
  @NonNull
  private final Receiver<View> recycler;
  private final boolean enableDiff;
  @NonNull
  private final Function<Object, Object> keyForItem;
  private final boolean detectMoves;
  @NonNull
  private WeakReference<Object> dataRef = new WeakReference<>(null);
  @NonNull
  private List items = emptyList();

  protected CompiledRepositoryPresenter(
      @NonNull final Function<Object, Integer> layoutId,
      @NonNull final Binder<Object, View> binder,
      @NonNull final Function<Object, Long> stableIdForItem,
      @NonNull final Receiver<View> recycler,
      final boolean enableDiff,
      @NonNull final Function<Object, Object> keyForItem,
      final boolean detectMoves,
      @NonNull final Function<Object, List<Object>> converter,
      @NonNull final Binder<Object, View> collectionBinder) {
    this.collectionBinder = collectionBinder;
    this.converter = converter;
    this.layoutId = layoutId;
    this.binder = binder;
    this.stableIdForItem = stableIdForItem;
    this.recycler = recycler;
    this.enableDiff = enableDiff;
    this.keyForItem = keyForItem;
    this.detectMoves = detectMoves;
  }

  @Override
  public int getItemCount(@NonNull final Object data) {
    return getItems(data).size();
  }

  @Override
  public int getLayoutResId(@NonNull final Object data, final int index) {
    return layoutId.apply(getItems(data).get(index));
  }

  @Override
  public void bind(@NonNull final Object data, final int index,
      @NonNull final RecyclerView.ViewHolder holder) {
    final Object item = getItems(data).get(index);
    final View view = holder.itemView;
    bind(data, item, view);
  }

  protected void bind(@NonNull final Object data, @NonNull final Object item,
      @NonNull final View view) {
    collectionBinder.bind(data, view);
    binder.bind(item, view);
  }

  @Override
  public void recycle(@NonNull final RecyclerView.ViewHolder holder) {
    recycler.accept(holder.itemView);
  }

  @Override
  public long getItemId(@NonNull final Object data, final int index) {
    return stableIdForItem.apply(getItems(data).get(index));
  }

  @NonNull
  private List getItems(@NonNull final Object data) {
    if (this.dataRef.get() != data) {
      items = converter.apply(data);
      this.dataRef = new WeakReference<>(data);
    }
    return items;
  }

  @Override
  public boolean getUpdates(@NonNull final Object oldData, @NonNull final Object newData,
      @NonNull final ListUpdateCallback listUpdateCallback) {
    if (!enableDiff) {
      return false;
    }

    if (oldData == newData) {
      // Consider this an additional observable update; send blanket change event
      final int itemCount = getItemCount(oldData);
      listUpdateCallback.onChanged(0, itemCount, null);
      return true;
    }

    // Do proper diffing.
    final List oldItems = getItems(oldData);
    final List newItems = getItems(newData); // This conveniently saves newData to dataRef.
    DiffUtil.calculateDiff(new DiffUtil.Callback() {
      @Override
      public int getOldListSize() {
        return oldItems.size();
      }

      @Override
      public int getNewListSize() {
        return newItems.size();
      }

      @Override
      public boolean areItemsTheSame(final int oldItemPosition, final int newItemPosition) {
        final Object oldKey = keyForItem.apply(oldItems.get(oldItemPosition));
        final Object newKey = keyForItem.apply(newItems.get(newItemPosition));
        return oldKey.equals(newKey);
      }

      @Override
      public boolean areContentsTheSame(final int oldItemPosition, final int newItemPosition) {
        final Object oldItem = oldItems.get(oldItemPosition);
        final Object newItem = newItems.get(newItemPosition);
        return oldItem.equals(newItem);
      }
    }, detectMoves).dispatchUpdatesTo(listUpdateCallback);
    return true;
  }
}
