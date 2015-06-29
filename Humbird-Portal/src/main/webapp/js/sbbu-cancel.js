$( document ).ready(function() {
    $('#cancelCheck').change(function(){
        if(this.checked){
            $( "#circuit" ).prop( "disabled", false );
            $( "#customerId" ).prop( "disabled", true );
            $( "#executionDate" ).prop( "disabled", true );
            $( "#circuitType" ).prop( "disabled", false );
            $( "#streetName" ).prop( "disabled", true );
            $( "#city" ).prop( "disabled", true );
            $( "#cancelOrderNumber" ).prop( "disabled", false );
        }else{
            $( "#circuit" ).prop( "disabled", false );
            $( "#customerId" ).prop( "disabled", false );
            $( "#executionDate" ).prop( "disabled", false );
            $( "#circuitType" ).prop( "disabled", false );
            $( "#streetName" ).prop( "disabled", false );
            $( "#city" ).prop( "disabled", false );
            $( "#cancelOrderNumber" ).prop( "disabled", true );
        }
    });
});