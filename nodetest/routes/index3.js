// routes/index3.js

module.exports = function(app, Id)
{
    app.post('/ids', function(req, res){
      var id = new Id(
      id.id = req.body.id;

      id.save(function(err){
        if (err){
          console.error(err);
          res.json({result: 0});
          return;
        }
        res.json({result: 1});
      });
    });

    app.get('/ids', function(req,res){
      Id.find(function(err, id){
        if(err) return res.status(500).send({error: 'database error'});
        res.json(id);
      });
    });

    app.get('/ids/:id', function(req,res){
      Id.find({id: req.params.id}, function(err, id){
        if(err) return res.status(500).send({error: 'database error'});
        res.json(id);
      });
    });
};
