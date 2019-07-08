// routes/index1.js

module.exports = function(app, Contact)
{
    app.post('/contacts', function(req, res){
        var contact = new Contact();
        contact.name = req.body.name;
        contact.phonenumber = req.body.phonenumber;
        contact.icon = req.body.icon;
        contact.contact_id = req.body.contact_id;
        contact.tag = req.body.tag;
        contact.email = req.body.email;

        contact.save(function(err){
            if(err){
                console.error(err);
                res.json({result: 0});
                return;
            }
            res.json({result: 1});
        });
    });

    // INITAILIZE
    app.post('/contacts/initialize', function(req, res){
      var jsonarr = req.body;
      var sendarr = [];
      jsonarr.some(function(element){
        Contact.create(element)
          .then(contact => sendarr.push(contact))
          .catch(err => {res.status(500).send(err);return true;});
      });
      res.send(sendarr);
    });


    // GET ALL CONTACTS
    app.get('/contacts', function(req,res){
        Contact.find(function(err, contacts){
            if(err) return res.status(500).send({error: 'database failure'});
            res.json(contacts);
        });
    });

    // GET ALL CONTACTS BY TAG AND EMAIL
    app.get('/contacts/tag/:tag/:email', function(req,res){
        Contact.find({tag: req.params.tag, email: req.params.email}, {_id: 0, name: 1, phonenumber: 1, icon: 1, contact_id: 1}, function(err, contacts){
            if(err) return res.status(500).send({error: 'database failure'});
            res.json(contacts);
        });
    });

    // GET SINGLE CONTACT
    app.get('/contacts/:contact_id', function(req, res){
        Contact.findOne({_id: req.params.contact_id}, function(err, contact){
            if(err) return res.status(500).json({error: err});
            if(!contact) return res.status(404).json({error: 'contact not found'});
            res.json(contact);
        });
    });

    // GET CONTACT BY NAME
    app.get('/contacts/name/:name', function(req, res){
        Contact.find({name: req.params.name}, {_id: 0, name: 1, phonenumber: 1, tag: 1, icon: 1}, function(err, contacts){
            if(err) return res.status(500).json({error: err});
            if(contacts.length === 0) return res.status(404).json({error: 'contact not found'});
            res.json(contacts);
        });
    });

    // GET CONTACT BY NAME ON TAG AND EMAIL
    app.get('/contacts/tag/:tag/name/:name/:email', function(req, res){
        Contact.find({tag: req.params.tag, name: req.params.name, email: req.params.email}, {_id: 0, name: 1, phonenumber: 1, icon : 1}, function(err, contacts){
            if(err) return res.status(500).json({error: err});
            if(contacts.length === 0) return res.status(404).json({error: 'contact not found'});
            res.json(contacts);
        });
    });

    // GET CONTACT BY PHONENUMBER ON TAG
    app.get('/contacts/tag/:tag/phonenumber/:phonenumber/:email', function(req, res){
        Contact.find({tag: req.params.tag, phonenumber: req.params.phonenumber, email: req.params.email}, {_id: 0, name: 1, phonenumber: 1, icon : 1}, function(err, contacts){
            if(err) return res.status(500).json({error: err});
            if(contacts.length === 0) return res.status(404).json({error: 'contact not found'});
            res.json(contacts);
        });
    });

        // UPDATE THE CONTACT BY ID
    app.put('/contacts/:contact_id', function(req, res){
        Contact.findById(req.params.contact_id, function(err, contact){
            if(err) return res.status(500).json({ error: 'database failure' });
            if(!contact) return res.status(404).json({ error: 'contact not found' });

            if(req.body.name) contact.name = req.body.name;
            if(req.body.phonenumber) contact.phonenumber = req.body.phonenumber;
            if(req.body.icon) contact.icon = req.body.icon;
            if(req.body.tag) contact.tag = req.body.tag;
            if(req.body.contact_id) contact.contact_id = req.body.contact_id;

            contact.save(function(err){
                if(err) res.status(500).json({error: 'failed to update'});
                res.json({message: 'contact updated'});
            });

        });
    });

    // UPDATE THE CONTACT ID by NAME PHONENUMBER
    app.patch('/contacts/update/name/:name/phonenumber/:phonenumber/tag/:tag/:email', function(req, res){
        Contact.findOne({name: req.params.name, phonenumber: req.params.phonenumber, tag: req.params.tag, email: req.params.email}, function(err, contact){
            if(err) return res.status(500).json({ error: 'database failure' });
            if(!contact) return res.status(404).json({ error: 'contact not found' });

            if(req.body.contact_id) contact.contact_id = req.body.contact_id;

            contact.save(function(err){
                if(err) res.status(500).json({error: 'failed to update'});
                res.json({message: 'contact updated'});
            });

        });
    });

    // // UPDATE THE BOOK (ALTERNATIVE)
    // app.put('/api/contacts/:book_id', function(req, res){
    //     Book.update({ _id: req.params.book_id }, { $set: req.body }, function(err, output){
    //         if(err) res.status(500).json({ error: 'database failure' });
    //         console.log(output);
    //         if(!output.n) return res.status(404).json({ error: 'book not found' });
    //         res.json( { message: 'book updated' } );
    //     })
    // });

    // DELETE CONTACT BY ID
    app.delete('/contacts/:contact_id', function(req, res){
        Contact.remove({ _id: req.params.contact_id }, function(err, output){
            if(err) return res.status(500).json({ error: "database failure" });

            /* ( SINCE DELETE OPERATION IS IDEMPOTENT, NO NEED TO SPECIFY )
            if(!output.result.n) return res.status(404).json({ error: "book not found" });
            res.json({ message: "book deleted" });
            */

            res.status(204).end();
        });
    });

    // DELETE CONTACT BY TAG AND phonenumber
    app.delete('/contacts/tag/:tag/phonenumber/:phonenumber/:email', function(req, res){
      Contact.deleteOne({tag: req.params.tag, phonenumber: req.params.phonenumber, email: req.params.email}, function(err, output){
        res.status(204).end();
      });
    });

    // DELETE CONTACT BY TAG
    app.delete('/contacts/tag/:tag/:email', function(req, res){
        Contact.deleteMany({tag: req.params.tag, email: req.params.email}, function(err, output){
             res.status(204).end();
        });
    });

};