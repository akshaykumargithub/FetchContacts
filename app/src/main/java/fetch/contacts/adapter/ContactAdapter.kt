package fetch.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fetch.contacts.databinding.ContactItemBinding
import fetch.contacts.model.Contact

class ContactAdapter : RecyclerView.Adapter<ContactViewHolder>() {
    var contacts: ArrayList<Contact>? = null
    fun setContactList(contacts: ArrayList<Contact>?) {
        this.contacts = contacts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ContactItemBinding.inflate(inflater, parent, false)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contactItem: Contact? = contacts?.get(position)
        holder.binding.contactName.text = contactItem?.contactName
        holder.binding.contactNumber.text = contactItem?.contactNumber
    }

    override fun getItemCount(): Int {
        return contacts!!.size
    }
}

class ContactViewHolder(val binding: ContactItemBinding) : RecyclerView.ViewHolder(binding.root) {}