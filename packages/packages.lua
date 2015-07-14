--
--   Clouseau
-- 
--    Copyright (C) 2015 Pavel Tisnovsky <ptisnovs@redhat.com>
-- 
-- Bytecode synth is free software; you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation; either version 2, or (at your option)
-- any later version.
-- 
-- Bytecode synth is distributed in the hope that it will be useful, but
-- WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
-- General Public License for more details.
-- 
-- You should have received a copy of the GNU General Public License
-- along with Bytecode synth; see the file COPYING.  If not, write to the
-- Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
-- 02110-1301 USA.
-- 
-- Linking this library statically or dynamically with other modules is
-- making a combined work based on this library.  Thus, the terms and
-- conditions of the GNU General Public License cover the whole
-- combination.
-- 
-- As a special exception, the copyright holders of this library give you
-- permission to link this library with independent modules to produce an
-- executable, regardless of the license terms of these independent
-- modules, and to copy and distribute the resulting executable under
-- terms of your choice, provided that you also meet, for each linked
-- independent module, the terms and conditions of the license of that
-- module.  An independent module is a module which is not derived from
-- or based on this library.  If you modify this library, you may extend
-- this exception to your version of the library, but you are not
-- obligated to do so. If you do not wish to do so, delete this
-- exception statement from your version.
-- 

local repoTable ={
}

local names = {
}



--
-- Tests if the given string ends with the specified suffix.
--
function string.endsWith(str, suffix)
    local substring = string.sub(str, -string.len(suffix))
    return suffix == "" or substring == suffix
end




--- Function which compose command and run it. This command
--  will check whether some file or directory exists or whether it is
--  readable or writeable.
--
-- @param file_path Path to the file which should be checked.
-- @param test_type Type of test. Posibilities:
--                                    "e" - exists
--                                    "r" - is readable
--                                    "w" - is writeable
--                                    "d" - is directory
--                                    "f" - is file
--                  It's possible to combine these types. e.c. "wre"
-- @return True when all conditions are true. Otherwise, false.
function checkFile(file_path, test_type)
  -- Table of correct values of test_type
  local types = {["e"]=true, ["r"]=true, ["w"]=true, ["d"]=true, ["f"]=true}

  -- Verify that the supplied file name is a string.
  if type(file_path) ~= "string" then
    return false
  end

  -- Get number of conditions.
  local type_lenght = string.len(test_type)

  -- Begin of command.
  local command = "[[ "

  -- Create condition for each letter in test_type.
  for i = 1, type_lenght, 1 do
    if i ~= 1 then
      command = command .. " && "
    end

    -- Get just one letter (operation) and check if it's correct.
    local operation = string.sub(test_type, i, i)
    if not types[operation] then
      fail("Bad second argument of checkFile() function: " .. operation .. ".")
      return false
    end

    -- Add new condition.
    local new_part = "( -" .. operation .. " " .. file_path .. " )"
    command = command .. new_part
  end

  -- Add ending of command.
  command = command .. " ]] && echo '1' || echo '0'"

  -- Execute command and capture its output.
  file_handle = assert(io.popen(command))

  -- Check if command went correctly.
  if file_handle:read() == "0" then
    file_handle:close()
    return false
  end

  -- Close file handler.
  file_handle:close()

  return true
end



--- Function which download file from URL to the target file.
--
-- @param file_path Link to the file which should be downloaded.
-- @param target Path to the new file.
-- @return Returns true when file has been stored succesfully. Otherwise, returns 0.
function downloadFile(file_link, target)
    -- Command which download the file.
    local command = "curl -f \"" .. file_link .. "\" > \"" .. target .."\""
 
    print("Running command: " .. command)
    -- Execute the command and capture its output.
    local file_handle = assert(io.popen(command))
 
    -- Close file handler
    file_handle:close()
 
    if not checkFile(target, "er") then
        fail(target .. " wasn't downloaded successfully.")
        return false
    end
 
    return true
end

--- Function which gets link to database file from repomd.xml file.
--
-- @param path Path to the repomd.xml file.
-- @return Ending of link of package database or nil when some error occured.
function getDatabaseLink(path)
    -- Create command for parse ending of link.
    local command = "xmlstarlet sel -N r='http://linux.duke.edu/metadata/repo' -t -v '/r:repomd/r:data[@type=\"primary_db\"]/r:location/@href' \"" .. path .."\""

    -- Check if repomd.xml exists and is readable.
    if not checkFile(path, "er") then
        fail("File repomd.xml doesn't exist or isn't readable.")
        return nil
    end

    -- Execute command
    local file_handle = assert(io.popen(command))

    -- Read first line of output
    local link_ending = file_handle:read()

    -- Close file handler
    file_handle:close()

    return link_ending
end

--- This function prepares directory where will be saved database and metadata files.
--
-- @param path Path to the directory, where will be saved database and metadata.
-- @return Returns list with two values. The first - True if dir already exists
--         or was successfully created. Otherwise false.
--         The second - True when directory were successfully created.
function prepareDir(path)
  -- Variable which is set when directory doesn't exist. In this case we are sure
  -- that files in this directory doesn't exist either.
  local output_list  = {["not_exist"]= false, ["is_ok"]=false}

  -- Check if folder exist.
  if not checkFile(path, "ed") then
    output_list.not_exist = true

    local command_maked = "mkdir -p " .. path

    -- Execute command and capture its output.
    local file_handle_create = assert(io.popen(command_maked))

    -- Close file handle
    file_handle_create:close()

    -- Check if directory was created succesfully
    if not checkFile(path, "ed") then
      fail("Error creating directory: " .. path)

      -- Set output
      output_list.is_ok = false
      return output_list
    end
  end

  output_list.is_ok = true
  return output_list
end

--- Function which gets all available packages from database file.
--
-- @param db_file_path Path to the file with package database.
-- @return Returns list of available packages. Or returns nil when some error occures.
function getAvailablePackages(db_file_path)
  -- Prepare empty list for packages
  local available_packages = {}

  -- Check if database file exists.
  if not checkFile(db_file_path, "er") then
    fail("File: " .. db_file_path .. "doesn't exist.")
    return nil
  end

  -- Compose command for getting all available packages
  local command = "sqlite3 \"" .. db_file_path .. "\" \"SELECT name FROM packages;\""

  local file_handle = assert(io.popen(command))

  for line in file_handle:lines() do
    --print("balicek: " .. line)
    table.insert(available_packages, line)
  end

  file_handle:close()

  -- Create new list ([item]=true, ...)
  available_packages = convertList(available_packages)

  return available_packages
end

--- Downloads and unzip database file.
--
-- @param db_link URL to database.
-- @param db_file_path Path to the file where should be database unziped.
-- @return Returns true if everything goes well. Otherwise, returns false.
function downloadAndUnzipDB(db_link, db_file_path)
    local downloadBZ2   = db_link:endsWith(".bz2")
    local downloadXZ    = db_link:endsWith(".xz")
    local unzip_command = nil
    local downloaded    = nil

    if downloadBZ2 then
        downloaded = downloadFile(db_link, db_file_path .. ".bz2")
    elseif downloadXZ then
        downloaded = downloadFile(db_link, db_file_path .. ".xz")
    else
        print("Don't know which file to download, exiting")
    end

    if not downloaded then
        fail("Download failed")
        return false
    end

    if downloadBZ2 then
        -- Compose command for unziping database and remove unuseful file.
        unzip_command = "bunzip2 -c \"" .. db_file_path .. ".bz2\" > \"" .. db_file_path .. "\" && rm -f \"" .. db_file_path .. ".bz2\""
    elseif downloadXZ then
        -- Compose command for unziping database and remove unuseful file.
        unzip_command = "unxz -c \"" .. db_file_path .. ".xz\" > \"" .. db_file_path .. "\" && rm -f \"" .. db_file_path .. ".xz\""
    else -- not needed :)
        fail("Download failed")
        return false
    end

    -- Execute command.
    file_handle_unzip = assert(io.popen(unzip_command))

    -- Close file handler
    file_handle_unzip:close()

    -- Check whether file was unziped correctly.
    if not checkFile(db_file_path, "er") then
        fail("Database file doesn't exist.")
        return false
    end
    return true
end

--- Function which compare found packages with available packages
--
-- @param list List of found packages.
function checkPackages(list)
  if #list == 0 then
    pass("No relevant commands found.")
    return
  end

  local found = false
  local package_list_counter = 0
  local all_repos = ""

  -- Cycle which compose the list of all used repositories. It is used when package isn't found.
  for i, repo in ipairs(namesOfRepositories) do
    if i > 1 then
      all_repos = all_repos .. ", " .. repo
    else
      all_repos = repo
    end
  end

  for i,package in ipairs(list) do

    found = false

    -- If package name is foo or foobar - it's probably not the name of real package.
    if package == "foo" or package:match(".*foobar.*") then
      warn(package .. " - This may be an example of package name.")

    -- If package name ends with *, it is necessary to test it using patterns.
    elseif package:match("%*$") then
      -- Store package name for printing.
      print_pckg = package

      -- Change pattern because of special characters.
      package = package:sub(1, #package - 1)
      package = package:gsub("%-", "%%-")

      package_list_counter = 0

      -- Compare current package with all list of available packages.
      for _, list_of_packages in ipairs(availablePackages) do
        -- If package was found in last comparsion, this cycle will end.
        if found then break end

        -- Incrementation of counter, needed because of getting name of current list from table.
        package_list_counter = package_list_counter + 1
        for real_pckg,_ in pairs(list_of_packages) do
          if real_pckg:match("^".. package .. ".*") then
            -- Set found as true and check whether current list of available packages is prioritized or not.
            found = true
            if repoTable[namesOfRepositories[package_list_counter]][2] == 1 then
              pass("Package **" .. print_pckg .. "** was found in " .. namesOfRepositories[package_list_counter] .. " repository.")
            else
              warn("Package **" .. print_pckg .. "** was found in " .. namesOfRepositories[package_list_counter] .. " repository.")
            end
            break
          end
        end
      end

      -- Save package name back to correct variable, because of printing fails.
      package = print_pckg
    else

      package_list_counter = 0

      -- Compare current package with all list of available packages.
      for _, list_of_packages in ipairs(availablePackages) do
        -- If package was found in last comparsion, this cycle will end.
        if found then break end

        -- Incrementation of counter, needed because of getting name of current list from table.
        package_list_counter = package_list_counter + 1
        if list_of_packages[package] == true then
           -- Set found as true and check whether current list of available packages is prioritized or not.
          found = true
          if repoTable[namesOfRepositories[package_list_counter]][2] == 1 then
            pass("Package **" .. package .. "** was found in " .. namesOfRepositories[package_list_counter] .. " repository.")
          else
            warn("Package **" .. package .. "** was found in " .. namesOfRepositories[package_list_counter] .. " repository.")
          end
        end
      end
    end

    -- If package wasn't found in any repository print it as fail.
    if not found then
      fail("Package **" .. package .. "** wasn't found in any of these repositories: " .. all_repos .. "."  )
    end
  end
end

function downloadRPMDatabases()
    for dirName, url in pairs(repoTable) do
        prepareDir(dirName)

        local mdFilePath = dirName .. "/repomd.xml"
        local dbFilePath = dirName .. "/primary.sqlite"
        local mdLink = url .. "/repodata/repomd.xml"

        if not downloadFile(mdLink, mdFilePath) then
            print("Error downloading the following file: " .. mdLink)
            break
        end

        local endingOfLink = getDatabaseLink(mdFilePath)
        if not endingOfLink then
            print("No primary_db node found, can not continue processing")
        else
            local dbLink = url .. "/" .. endingOfLink
            downloadAndUnzipDB(dbLink, dbFilePath)
        end
    end
end

function findKeyForName(name1)
    for key, name2 in pairs(names) do
        if name1 == name2 then
            return key
        end
    end
    return nil
end

function createProductsClj()
    local sortedNames = {}

    for _, name in pairs(names) do
        table.insert(sortedNames, name)
    end

    table.sort(sortedNames)

    local fout = io.open("products.clj", "w")
    fout:write("(ns clouseau.products)\n\n")
    fout:write("(def products [\n")

    for _, name in ipairs(sortedNames) do
        local key = findKeyForName(name)
        local url = repoTable[key]
        fout:write("     [\"" .. name .. "\"\n")
        fout:write("        {:classname   \"org.sqlite.JDBC\"\n")
        fout:write("         :subprotocol \"sqlite\"\n")
        fout:write("         :subname     \"packages/" .. key .. "/primary.sqlite\"\n")
        fout:write("     }]\n")
    end
    fout:write("])\n\n")

    fout:close()
end

function main()
    downloadRPMDatabases()
    createProductsClj()
end

main()

