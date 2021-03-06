/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.ui.repo;

import static com.github.mobile.Intents.EXTRA_USER;
import static com.github.mobile.RequestCodes.REPOSITORY_VIEW;
import static com.github.mobile.ResultCodes.RESOURCE_CHANGED;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.mobile.R.string;
import com.github.mobile.core.ResourcePager;
import com.github.mobile.ui.PagedItemFragment;
import com.google.inject.Inject;

import java.util.List;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.RepositoryService;

/**
 * Fragment to display a list of repositories for a {@link User}
 */
public class UserRepositoryListFragment extends PagedItemFragment<Repository> {

    @Inject
    private RepositoryService service;

    private User user;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        user = getSerializableExtra(EXTRA_USER);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(string.no_repositories);
    }

    @Override
    protected ResourcePager<Repository> createPager() {
        return new ResourcePager<Repository>() {

            @Override
            protected Object getId(Repository resource) {
                return resource.getId();
            }

            @Override
            public PageIterator<Repository> createIterator(int page, int size) {
                return service.pageRepositories(user.getLogin(), page, size);
            }
        };
    }

    @Override
    protected int getLoadingMessage() {
        return string.loading_repositories;
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return string.error_repos_load;
    }

    @Override
    protected SingleTypeAdapter<Repository> createAdapter(List<Repository> items) {
        return new UserRepositoryListAdapter(getActivity().getLayoutInflater(),
                items.toArray(new Repository[items.size()]), user);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REPOSITORY_VIEW && resultCode == RESOURCE_CHANGED) {
            forceRefresh();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onListItemClick(ListView list, View v, int position, long id) {
        Repository repo = (Repository) list.getItemAtPosition(position);
        startActivityForResult(RepositoryViewActivity.createIntent(repo),
                REPOSITORY_VIEW);
    }
}
