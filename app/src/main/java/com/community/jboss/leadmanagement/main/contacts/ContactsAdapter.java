package com.community.jboss.leadmanagement.main.contacts;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.community.jboss.leadmanagement.CustomDialogBox;
import com.community.jboss.leadmanagement.R;
import com.community.jboss.leadmanagement.data.daos.ContactNumberDao;
import com.community.jboss.leadmanagement.data.entities.Contact;
import com.community.jboss.leadmanagement.main.contacts.editcontact.EditContactActivity;
import com.community.jboss.leadmanagement.utils.DbUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {
    private List<Contact> mContacts;
    private ContactsAdapter mAdapter;
    public AdapterListener mListener;

    public ContactsAdapter(AdapterListener listener) {
        mListener = listener;
        mAdapter = this;
        mContacts = new ArrayList<>();
    }

    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_cell, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Contact contact = mContacts.get(position);
        holder.bind(contact);
    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    public void replaceData(List<Contact> contacts) {
        mContacts = contacts;
        notifyDataSetChanged();
    }

    public interface AdapterListener {
        void onContactDeleted(Contact contact);
    }

    final class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        @BindView(R.id.contact_name)
        TextView name;
        @BindView(R.id.contact_number)
        TextView number;
        @BindView(R.id.contact_delete)
        ImageButton deleteButton;


        private Contact mContact;
        private Context mContext;

        ViewHolder(View v) {
            super(v);

            mContext = v.getContext();

            ButterKnife.bind(this, v);

            v.setOnClickListener(this);
            v.setOnLongClickListener(this);

            deleteButton.setOnClickListener(v1 -> {
                CustomDialogBox dialogBox = new CustomDialogBox();
                dialogBox.showAlert((Activity) mContext,mContact,mAdapter);
            });
        }

        void bind(Contact contact) {
            mContact = contact;

            // TODO add contact avatar
            name.setText(contact.getName());
            number.setText(getNumber());
        }

        /**
         * TODO:
         * This really sucks but it'll do until we decide to make
         * database transactions go into the background thread,
         * or find out how to embed the contact number into the
         * contact object itself
         */
        private String getNumber() {
            final ContactNumberDao dao = DbUtil.contactNumberDao(mContext);
            return dao.getContactNumbers(mContact.getId()).get(0).getNumber();
        }

        @Override
        public void onClick(View view) {
            final Context context = view.getContext();

            Dialog detailDialog;
            detailDialog = new Dialog(context);

            TextView txtClose;
            TextView popupName;
            TextView contactNum;
            Button btnEdit;
            Button btnCall;
            Button btnMsg;

            detailDialog.setContentView(R.layout.popup_detail);
            txtClose = detailDialog.findViewById(R.id.txt_close);
            btnEdit = detailDialog.findViewById(R.id.btn_edit);
            popupName = detailDialog.findViewById(R.id.popup_name);
            contactNum = detailDialog.findViewById(R.id.txt_num);
            btnCall = detailDialog.findViewById(R.id.btn_call);
            btnMsg = detailDialog.findViewById(R.id.btn_msg);

            popupName.setText(name.getText());
            contactNum.setText(number.getText());

            txtClose.setOnClickListener(view1 -> detailDialog.dismiss());

            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Intent intent = new Intent(context, EditContactActivity.class);
                    intent.putExtra(EditContactActivity.INTENT_EXTRA_CONTACT_ID, mContact.getId());
                    context.startActivity(intent);
                }
            });


            btnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + number.getText().toString()));
                        context.startActivity(intent);
                }
            });

            btnMsg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"
                            + number.getText().toString())));
                }
            });

            detailDialog.show();



        }

        @Override
        public boolean onLongClick(View view) {
            final int newVisibility = deleteButton.getVisibility() == View.VISIBLE
                    ? View.GONE
                    : View.VISIBLE;
            deleteButton.setVisibility(newVisibility);
            return true;
        }
    }
}

