/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.agera.rvadapter;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import android.view.View;

/**
 * Contains logic to present a layout in a {@link RepositoryAdapter}.
 */
public abstract class LayoutPresenter {
  /**
   * Returns the layout resource ID to inflate.
   */
  @LayoutRes
  public abstract int getLayoutResId();

  /**
   * Updates the view to present. The view is inflated from the layout resource specified by
   * {@link #getLayoutResId}, but may have been previously updated with a different presenter.
   * Therefore, implementation should take care of resetting the view state.
   *
   * @param view The view to present.
   */
  public abstract void bind(@NonNull final View view);

  /**
   * Called when the given {@code view} is recycled.
   *
   * @param view The view to recycle.
   */
  public void recycle(@NonNull final View view) {}
}
