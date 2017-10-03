package crb_web_data

// This file was generated by the swagger tool.
// Editing this file might prove futile when you re-run the swagger generate command

import (
	"net/http"

	"github.com/go-openapi/errors"
	"github.com/go-openapi/runtime/middleware"

	strfmt "github.com/go-openapi/strfmt"
)

// NewRetrieveCopyParams creates a new RetrieveCopyParams object
// with the default values initialized.
func NewRetrieveCopyParams() RetrieveCopyParams {
	var ()
	return RetrieveCopyParams{}
}

// RetrieveCopyParams contains all the bound params for the retrieve copy operation
// typically these are obtained from a http.Request
//
// swagger:parameters retrieveCopy
type RetrieveCopyParams struct {

	// HTTP Request Object
	HTTPRequest *http.Request

	/*The id of the copy instance.
	  Required: true
	  In: path
	*/
	CopyID string
}

// BindRequest both binds and validates a request, it assumes that complex things implement a Validatable(strfmt.Registry) error interface
// for simple values it will use straight method calls
func (o *RetrieveCopyParams) BindRequest(r *http.Request, route *middleware.MatchedRoute) error {
	var res []error
	o.HTTPRequest = r

	rCopyID, rhkCopyID, _ := route.Params.GetOK("copyId")
	if err := o.bindCopyID(rCopyID, rhkCopyID, route.Formats); err != nil {
		res = append(res, err)
	}

	if len(res) > 0 {
		return errors.CompositeValidationError(res...)
	}
	return nil
}

func (o *RetrieveCopyParams) bindCopyID(rawData []string, hasKey bool, formats strfmt.Registry) error {
	var raw string
	if len(rawData) > 0 {
		raw = rawData[len(rawData)-1]
	}

	o.CopyID = raw

	return nil
}