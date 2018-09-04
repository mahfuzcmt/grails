<div class="section-seat-view" sectionId="${section.id}">
    <g:set var="rowAsc" value="${section.rowPrefixOrder == 'ascending'}"/>
    <g:set var="columnAsc" value="${section.columnPrefixOrder == 'ascending'}"/>
    <g:set var="rowAlpha" value="${section.rowPrefixType == 'alphabetic'}"/>
    <g:set var="columnAlpha" value="${section.columnPrefixType == 'alphabetic'}"/>
    <g:set var="rowIndexMultiplier" value="${rowAsc ? 1 : -1}"/>
    <g:set var="columnIndexMultiplier" value="${columnAsc ? 1 : -1}"/>
    <g:set var="row" value="${section.rowCount}"/>
    <g:set var="column" value="${section.columnCount}"/>
    <g:set var="columnAccess" value="${section.columnAccessBetween ?: row}"/>
    <g:set var="rowAccess" value="${section.rowAccessBetween ?: column}"/>
    <g:set var="totalRowDiv" value="${row + Math.ceil(row / rowAccess).intValue() + 1}"/>
    <g:set var="totalColumnDiv" value="${column + Math.ceil(column / columnAccess).intValue() + 1}"/>
    <g:set var="currentSeatNumber" value="${0}"/>
    <g:if test="${lockedTickets}">
        <g:set var="lockIterator" value="${lockedTickets.iterator()}"/>
        <g:set var="currentLock" value="${lockIterator.next()}"/>
    </g:if>
    <g:each in="${0..totalRowDiv-1}" var="i">
        <div class="seat-row">
            <g:set var="rowIndex" value="${i - Math.ceil((i + 1) / (rowAccess + 1)).intValue()}"/>
            <g:each in="${0..totalColumnDiv-1}" var="j"><span class='
                <g:if test="${ !i || (i % (rowAccess + 1) == 0) || i == totalRowDiv - 1 }">
                    <g:set var="columnIndex" value="${j - Math.ceil((j + 1) / (columnAccess + 1)).intValue()}"/>
                    <g:if test="${!i && (j % (columnAccess + 1)) && j != totalColumnDiv - 1}">
                        <g:set var="actualNumber" value="${columnNumber + columnIndexMultiplier * columnIndex}"/>
                        pathway'>${columnAlpha ? generalEventApp.decimalToAlphabet(number: actualNumber) : actualNumber}
                    </g:if>
                    <g:else>
                        pathway'>&nbsp;
                    </g:else>
                </g:if>
                <g:else>
                    <g:if test="${ !j || (j % (columnAccess + 1) == 0) || j == totalColumnDiv - 1 }">
                        <g:if test="${!j && (i % (rowAccess + 1)) && i != totalRowDiv - 1}">
                            <g:set var="actualNumber" value="${rowNumber + rowIndexMultiplier * rowIndex}"/>
                            pathway'>${rowAlpha ? generalEventApp.decimalToAlphabet(number: actualNumber) : actualNumber}
                        </g:if>
                        <g:else>
                            pathway'>&nbsp;
                        </g:else>
                    </g:if>
                    <g:else>
                        <g:set var="currentSeatNumber" value="${currentSeatNumber + 1}"/>
                        <g:if test="${currentLock && currentLock == currentSeatNumber}">
                            <g:if test="${lockIterator.hasNext()}">
                                <g:set var="currentLock" value="${lockIterator.next()}"/>
                            </g:if>
                            <g:else>
                                <g:set var="currentLock" value="${null}"/>
                            </g:else>
                            <g:set var="clazz" value="booked"/>
                        </g:if>
                        <g:else>
                            <g:if test="${orderedQuantity}">
                                <g:set var="clazz" value="selected"/>
                                <g:set var="orderedQuantity" value="${orderedQuantity - 1}"/>
                            </g:if>
                            <g:else>
                                <g:set var="clazz" value=""/>
                            </g:else>
                        </g:else>
                        seat ${clazz}' seat-number='${currentSeatNumber}'>&nbsp;
                    </g:else>
                </g:else>
            </span></g:each>
        </div>
    </g:each>
</div>