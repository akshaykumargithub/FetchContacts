package fetch.contacts.ui

import android.Manifest
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.opengl.Visibility
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import fetch.contacts.model.Contact
import fetch.contacts.ContactAdapter
import fetch.contacts.R
import fetch.contacts.databinding.ActivityMainBinding

class ContactBook : AppCompatActivity() {
    private val TAG: String? = ContactBook::class.qualifiedName
    private lateinit var binding: ActivityMainBinding
    private val RECORD_REQUEST_CODE = 101
    private var permission: Int? = null

    private val adapter = ContactAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        permission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
        getSupportActionBar()?.setTitle(getString(R.string.contactBook))
        if (permission == PackageManager.PERMISSION_GRANTED) {
            binding.phoneBookTitle.text = getString(R.string.phonebook_granted)
            binding.givePermision.text = getString(R.string.fetchContacts)
        }

        binding.givePermision.setOnClickListener(View.OnClickListener {
            if (permission != PackageManager.PERMISSION_GRANTED) {
                makeRequest()
            } else {
                binding.givePermision.text = getString(R.string.gettingContactsInBackground)
                binding.loader.visibility = View.VISIBLE
                adapter.setContactList(getContacts())
            }
        })
        binding.contactList.adapter = adapter
        binding.addContact.setOnClickListener(View.OnClickListener {
            openDialog()
        })

    }

    private fun openDialog() {
        val addContactDialog: AlertDialog.Builder = AlertDialog.Builder(this)
        var contactDialog: AlertDialog? = null;
        val addContactView: View = layoutInflater.inflate(R.layout.add_contact_layout, null, false)
        addContactDialog.setTitle(getString(R.string.addNewContact))
        addContactDialog.setCancelable(false)
        addContactDialog.setView(addContactView)
        val contactName = addContactView.findViewById(R.id.contactName) as EditText
        val contactNumber = addContactView.findViewById(R.id.contactNumber) as EditText
        val submitBtn = addContactView.findViewById(R.id.submitBtn) as Button
        val cancelBtn = addContactView.findViewById(R.id.cancelBtn) as Button
        submitBtn.setOnClickListener(View.OnClickListener {
            if (contactName.text.isNotBlank()) {
                if (contactNumber.text.isNotBlank()) {
                    //Here we can match number with regex or check the lenght of phone number
                    var name: String = contactName.text.toString()
                    var number: String = contactNumber.text.toString()
                    adapter.contacts!!.add(0, Contact(name, number))
                    adapter.notifyDataSetChanged()
                    contactDialog?.dismiss()
                } else
                    contactNumber.setError(getString(R.string.enterNumberPlease))
            } else
                contactName.setError(getString(R.string.enterNamePlease))
        })
        contactDialog = addContactDialog.create()
        contactDialog.show()
        cancelBtn.setOnClickListener(View.OnClickListener { contactDialog.dismiss() })
    }

    private fun getContacts(): ArrayList<Contact> {
        var list: ArrayList<Contact> = ArrayList();
        Thread() {
            val resolver: ContentResolver = contentResolver;
            val cursor = resolver.query(
                ContactsContract.Contacts.CONTENT_URI, null, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
            if (cursor!!.count > 0) {
                while (cursor.moveToNext()) {
                    val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                    val name =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    val phoneNumber =
                        (cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))).toInt()
                    if (phoneNumber > 0) {
                        val cursorPhone = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                            arrayOf(id),
                            null
                        )
                        if (cursorPhone!!.count > 0) {
                            while (cursorPhone.moveToNext()) {
                                val phoneNumValue =
                                    cursorPhone!!.getString(
                                        cursorPhone.getColumnIndex(
                                            ContactsContract.CommonDataKinds.Phone.NUMBER
                                        )
                                    )
                                list!!.add(Contact(name, phoneNumValue))
                            }
                        }
                        cursorPhone.close()
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.noContacts), Toast.LENGTH_LONG).show()
            }
            cursor.close()
            runOnUiThread() {
                updateUI()
            }
        }.start()

        return list
    }

    private fun updateUI() {
        binding.phoneBookTitle.visibility = View.GONE
        binding.phoneBookImg.visibility = View.GONE
        binding.givePermision.visibility = View.GONE
        binding.contactList.visibility = View.VISIBLE
        binding.addContact.visibility = View.VISIBLE
        binding.loader.visibility = View.GONE
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_CONTACTS),
            RECORD_REQUEST_CODE
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            101 -> {
                if (grantResults[0] === PackageManager.PERMISSION_GRANTED
                ) {
                    binding.loader.visibility = View.VISIBLE
                    binding.phoneBookTitle.visibility = View.GONE
                    binding.givePermision.text = getString(R.string.gettingContactsInBackground)
                    adapter.setContactList(getContacts())
                } else {
                    Toast.makeText(
                        this@ContactBook,
                        getString(R.string.pleaseIsRequiredToProcessFurther),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }
}
