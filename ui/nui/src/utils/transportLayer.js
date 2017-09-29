import $ from 'jquery';

const request = (options, success, error) => {
  $.ajax({
    url: options.url,
    data: JSON.stringify(options.data),
    success: success,
    error: error
  })
}
