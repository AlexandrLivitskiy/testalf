model.endpoints = [];

try {
    var ids = remote.getEndpointIds();

    if (ids && ids.length > 0) {
        for each (var id in ids) {
            try {
                var ep = remote.connect(id);
                model.endpoints.push({
                    id: id,
                    exists: ep != null
                });
            } catch(e) {
                model.endpoints.push({
                    id: id,
                    exists: false,
                    error: e.message
                });
            }
        }
    } else {
        model.endpoints.push({
            id: "(none)",
            exists: false,
            error: "No endpoints defined or remote service unavailable"
        });
    }

} catch(e) {
    model.endpoints.push({
        id: "(none)",
        exists: false,
        error: "Failed to retrieve endpoint IDs: " + e.message
    });
}
