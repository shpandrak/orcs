import Reflux from 'reflux';

var Actions = Reflux.createActions([
    
    "userClickOnAddNewIdea",
	"uploadImage",
	"setIdeaName",
	"setIdeaDescription",
	"goToMainScreen",
	"resetImageName",
    "hideErrorDialog",
    "showErrorDialog",
    "setLocationHash",
    "populateHash",
    "navigate",
    "userClickOnIdea",
    "userClickOnVote",
    "enlargeIdeaDetailImage",
    "minimizeIdeaDetailsImage"
])

export default Actions;