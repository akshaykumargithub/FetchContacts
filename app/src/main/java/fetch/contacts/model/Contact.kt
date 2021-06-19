package fetch.contacts.model

class Contact {
    var     contactName: String? = null
    var contactNumber: String? = null

    constructor(contactMame: String?, contactNumber: String?) {
        this.contactName = contactMame
        this.contactNumber = contactNumber
    }


}