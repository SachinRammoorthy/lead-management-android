package com.community.jboss.leadmanagement.main.contacts;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.community.jboss.leadmanagement.R;
import com.community.jboss.leadmanagement.data.entities.Contact;
import com.community.jboss.leadmanagement.main.MainActivity;
import com.community.jboss.leadmanagement.main.MainFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ContactsFragment extends MainFragment implements ContactsAdapter.AdapterListener {

    @BindView(R.id.contact_recycler)
    RecyclerView recyclerView;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeToRefresh;
    @BindView(R.id.search_name)
    EditText nameText;
    @BindView(R.id.text_test)
    TextView textView;
    @BindView(R.id.card_view)
    CardView cardView;

    private Unbinder mUnbinder;
    private ContactsFragmentViewModel mViewModel;
    private ContactsAdapter mAdapter;
    List<Contact> mContacts;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        mUnbinder = ButterKnife.bind(this, view);

        mViewModel = ViewModelProviders.of(this).get(ContactsFragmentViewModel.class);
        mViewModel.getContacts().observe(this, contacts -> {
            mAdapter.replaceData(contacts);
        });

        final MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.initFab();
        }

        setHasOptionsMenu(true);
        textView.setVisibility(View.GONE);

        mAdapter = new ContactsAdapter(this);
        recyclerView.setAdapter(mAdapter);

        swipeToRefresh.setOnRefreshListener(() -> {
            mAdapter.replaceData(mViewModel.getContacts().getValue());
            swipeToRefresh.setRefreshing(false);
        });

        cardView.setVisibility(View.GONE);
        nameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mAdapter.getFilter().filter(charSequence.toString());
                if (mAdapter.sizeOfArray == 0){
                    textView.setVisibility(View.VISIBLE);
                }
                else {
                    textView.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.contact_frag, menu);

        ImageButton searchIcon = (ImageButton) menu.findItem(R.id.search_icon).getActionView();
        searchIcon.setImageResource(R.drawable.ic_search_white_24dp);
        searchIcon.setBackgroundColor(Color.parseColor("#3F51B5"));

        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleView(view);
            }
        });

        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mUnbinder.unbind();
    }

    @Override
    public int getTitle() {
        return R.string.title_contacts;
    }

    @Override
    public void onContactDeleted(Contact contact) {
        mViewModel.deleteContact(contact);
    }

    public void toggleView(View view){
        if(cardView.getVisibility()==View.GONE)
            cardView.setVisibility(View.VISIBLE);
        else if(cardView.getVisibility()==View.VISIBLE)
            cardView.setVisibility(View.GONE);
    }
}
