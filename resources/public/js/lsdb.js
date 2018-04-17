function LSDB(name) {
    var ls = window.localStorage;
    var _name = name;
    var _prefix = "__LSDB__" + _name;
    var _sep = "/";
    var funcs = {
        get: function(key, default_val) {
            var k = _prefix + _sep + key;
            var val = ls.getItem(k);
            if (val === null && default_val) {
                for (var idx = 0; idx < ls.length; idx++) {
                    if (ls.key(idx) === k)
                        return null;
                }
                return default_val;
            }
            return JSON.parse(val);
        },

        set: function(key, value) {
            var k = _prefix + _sep + key;
            var v = JSON.stringify(value);
            ls.setItem(k,v);
        },

        remove: function(key) {
            ls.removeItem(_prefix + _sep + key);
        },

        keys: function() {
            var keys = [];
            for (var idx = 0; idx < ls.length; idx++) {
                var key = ls.key(idx);
                var pfx = key.split(_sep)[0];
                if (pfx === _prefix) {
                    keys.push(key.slice(pfx.length + _sep.length));
                }
            }
            return keys;
        },

        values: function() {
            var values = [];
            for (var idx = 0; idx < ls.length; idx++) {
                var key = ls.key(idx);
                var pfx = key.split(_sep)[0];
                if (pfx === _prefix) {
                    values.push(JSON.parse(ls.getItem(key)));
                }
            }
            return values;
        },

        clear: function() {
            var keys = this.keys();
            for (var idx = 0; idx < keys.length; idx++) {
                ls.removeItem(_prefix + _sep + keys[idx]);
            }
        },

        kill: function() {
            this.clear();
        },

        getName:   function() { return  _name; }
    };

    this.get = function(key, default_val) { return funcs.get(key,default_val); };
    this.set = function(key, value) { return funcs.set(key,value); };
    this.remove = function(key) { return funcs.remove(key); };
    this.keys = function() { return funcs.keys(); };
    this.values = function() { return funcs.values(); };
    this.clear = function() { return funcs.clear(); };
    this.kill = function() { return funcs.kill(); };
    this.getName = function() { return funcs.getName(); };
};
