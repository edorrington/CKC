// Shamelessly cribbed from JQuery UI
$.fn.shake = function( o, done ) {
    var el = $( this ),
        o = o || {},
        done = done || function(){},
        props = [ "position", "top", "bottom", "left", "right", "height", "width" ],
        direction = o.direction || "left",
        distance = o.distance || 20,
        duration = o.duration || 200,
        easing = o.easing || "swing",
        times = o.times || 3,
        anims = times * 2 + 1,
        speed = Math.round( duration / anims ),
        ref = (direction === "up" || direction === "down") ? "top" : "left",
        positiveMotion = (direction === "up" || direction === "left"),
        animation = {},
        animation1 = {},
        animation2 = {},
        i,

        vis = el.is(":visible"),
        pvals = props.reduce(function(o,prop) { o[prop] = el.css(prop); return o; },{}),

        // we will need to re-assemble the queue to stack our animations in place
        queue = el.queue(),
        queuelen = queue.length;

    el.show();  // Make sure the element is visible!

    // Animation
    animation[ ref ] = ( positiveMotion ? "-=" : "+=" ) + distance;
    animation1[ ref ] = ( positiveMotion ? "+=" : "-=" ) + distance * 2;
    animation2[ ref ] = ( positiveMotion ? "-=" : "+=" ) + distance * 2;

    // Animate
    el.animate( animation, speed, easing );

    // Shakes
    for ( i = 1; i < times; i++ ) {
        el.animate( animation1, speed, easing ).animate( animation2, speed, easing );
    }
    el
        .animate( animation1, speed, easing )
        .animate( animation, speed / 2, easing )
        .queue(function(next) {
            if ( !vis ) {
                el.hide();
            }
            for (var prop in pvals) {
                el.css(prop,pvals[prop]);
            }
            done();
            next();
        });

    // inject all the animations we just queued to be first in line (after "inprogress")
    if ( queuelen > 1) {
        queue.splice.apply( queue,
            [ 1, 0 ].concat( queue.splice( queuelen, anims + 1 ) ) );
    }
    return el;
};
