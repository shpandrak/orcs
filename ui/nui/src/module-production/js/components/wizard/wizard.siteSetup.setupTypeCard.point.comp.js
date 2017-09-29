import React from 'react';


let Point = React.createClass({
    
    render: function(){
        
        //console.log(this.props);
        return(
            <div className="point">
                <div className="point-inside"></div>
            </div>
        )
    },
});

export default Point;