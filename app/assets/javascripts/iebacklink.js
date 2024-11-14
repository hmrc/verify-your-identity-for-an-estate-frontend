$(document).ready(function() {
alert('here')
    // =====================================================
    // Back link mimics browser back functionality
    // =====================================================
    // store referrer value to cater for IE - https://developer.microsoft.com/en-us/microsoft-edge/platform/issues/10474810/  */
    var docReferrer = document.referrer
    // prevent resubmit warning
    if (window.history && window.history.replaceState && typeof window.history.replaceState === 'function') {

    alert('here 1')
        window.history.replaceState(null, null, window.location.href);
    }
    $('#back-link').on('click', function(e){
    alert('here 2')
        e.preventDefault();
        if (window.location.href.indexOf("main-content") !== -1) {
            window.history.go(-2);
        } else {
            window.history.back();
        }
    });
});


