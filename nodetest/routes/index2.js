// routes/index2.js

module.exports = function(app, Image)
{
    app.post('/api/images', function(req, res){
        var image = new Image();
        image.title = req.body.title;
        image.picture = req.body.picture;
        image.tag = req.body.tag;

        image.save(function(err){
            if(err){
                console.error(err);
                res.json({result: 0});
                return;
            }
            res.json({result: 1});
        });
    });

    // INITAILIZE
    app.post('/api/images/initialize', function(req, res){
      var jsonarr = req.body;
      var sendarr = [];
      jsonarr.some(function(element){
        Image.create(element)
          .then(image => sendarr.push(image))
          .catch(err => {res.status(500).send(err);return true;});
      });
      res.send(sendarr);
    });

    // GET ALL IMAGES
    app.get('/api/images', function(req,res){
        Image.find(function(err, images){
            if(err) return res.status(500).send({error: 'database failure'});
            res.json(images);
        });
    });

    // GET ALL IMAGES BY TAG
    app.get('/api/images/tag/:tag', function(req,res){
        Image.find({tag: req.params.tag}, {_id: 0, title: 1, picture: 1}, function(err, images){
            if(err) return res.status(500).send({error: 'database failure'});
            res.json(images);
        });
    });

    // GET SINGLE IMAGE
    app.get('/api/images/:image_id', function(req, res){
        Image.findOne({_id: req.params.image_id}, function(err, image){
            if(err) return res.status(500).json({error: err});
            if(!image) return res.status(404).json({error: 'image not found'});
            res.json(image);
        });
    });

    // GET IMAGE BY TITLE
    app.get('/api/images/title/:title', function(req, res){
        Image.find({title: req.params.title}, {_id: 0, title: 1, picture: 1, tag: 1}, function(err, images){
            if(err) return res.status(500).json({error: err});
            if(images.length === 0) return res.status(404).json({error: 'image not found'});
            res.json(images);
        });
    });

    // GET IMAGE BY TITLE ON TAG
    app.get('/api/images/tag/:tag/title/:title', function(req, res){
        Image.find({tag: req.params.tag, title: req.params.title}, {_id: 0, title: 1, picture: 1}, function(err, images){
            if(err) return res.status(500).json({error: err});
            if(images.length === 0) return res.status(404).json({error: 'image not found'});
            res.json(images);
        });
    });

        // UPDATE THE IMAGE BY ID
    app.put('/api/images/:image_id', function(req, res){
        Image.findById(req.params.image_id, function(err, image){
            if(err) return res.status(500).json({ error: 'database failure' });
            if(!image) return res.status(404).json({ error: 'image not found' });

            if(req.body.title) image.title = req.body.title;
            if(req.body.picture) image.picture = req.body.picture;
            if(req.body.tag) image.tag = req.body.tag;

            image.save(function(err){
                if(err) res.status(500).json({error: 'failed to update'});
                res.json({message: 'image updated'});
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

    // DELETE IMAGE BY ID
    app.delete('/api/images/:image_id', function(req, res){
        Image.remove({ _id: req.params.image_id }, function(err, output){
            if(err) return res.status(500).json({ error: "database failure" });

            /* ( SINCE DELETE OPERATION IS IDEMPOTENT, NO NEED TO SPECIFY )
            if(!output.result.n) return res.status(404).json({ error: "book not found" });
            res.json({ message: "book deleted" });
            */

            res.status(204).end();
        });
    });

};